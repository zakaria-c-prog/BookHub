package edu.njust.bookhub.service;

import edu.njust.bookhub.dao.AuthorDao;
import edu.njust.bookhub.dao.BookDao;
import edu.njust.bookhub.dao.ProviderDao;
import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.BookSearchCriteria;
import edu.njust.bookhub.model.CatalogStats;
import edu.njust.bookhub.model.CatalogStats.CategoryCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Book catalogue operations: searching, maintaining titles, stock and pricing. */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BookDao bookDao;
    private final AuthorDao authorDao;
    private final ProviderDao providerDao;

    public CatalogService(BookDao bookDao, AuthorDao authorDao, ProviderDao providerDao) {
        this.bookDao = bookDao;
        this.authorDao = authorDao;
        this.providerDao = providerDao;
    }

    // ================================================================= lookups

    public List<Book> search(BookSearchCriteria criteria) {
        return bookDao.search(criteria);
    }

    public Book getBook(int bookId) {
        return bookDao.findById(bookId).orElseThrow(() -> new NotFoundException("Book", bookId));
    }

    public List<Book> booksByAuthor(int authorId) {
        return bookDao.findByAuthor(authorId);
    }

    public List<Book> booksByProvider(int providerId) {
        return bookDao.findByProvider(providerId);
    }

    public List<Book> lowStockReport() {
        return bookDao.findLowStock(Book.LOW_STOCK_THRESHOLD);
    }

    public List<Book> recentlyAdded(int limit) {
        return bookDao.findRecentlyAdded(limit);
    }

    public List<String> categories() {
        return bookDao.findCategories();
    }

    public CatalogStats stats() {
        List<CategoryCount> categories = bookDao.countByCategory();
        List<Book> all = bookDao.search(new BookSearchCriteria());
        int units = all.stream().mapToInt(Book::getStock).sum();
        BigDecimal value = all.stream()
                .map(b -> b.getPrice().multiply(BigDecimal.valueOf(b.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CatalogStats(all.size(), authorDao.count(), providerDao.count(), units, value, categories);
    }

    // ================================================================= updates

    @Transactional
    public int addBook(Book book, List<Integer> authorIds) {
        checkIsbnFree(book.getIsbn(), null);
        return bookDao.insert(book, authorIds);
    }

    @Transactional
    public void updateBook(Book book, List<Integer> authorIds) {
        getBook(book.getBookId());
        checkIsbnFree(book.getIsbn(), book.getBookId());
        bookDao.update(book, authorIds);
    }

    @Transactional
    public void deleteBook(int bookId) {
        if (!bookDao.delete(bookId)) {
            throw new NotFoundException("Book", bookId);
        }
    }

    /** Receives new copies from the provider. */
    @Transactional
    public void restock(int bookId, int copies) {
        if (copies <= 0) {
            throw new BusinessRuleException("Number of copies to receive must be positive.");
        }
        getBook(bookId);
        bookDao.adjustStock(bookId, copies);
    }

    /** Records copies sold; refuses to sell more than are in stock. */
    @Transactional
    public void recordSale(int bookId, int copies) {
        if (copies <= 0) {
            throw new BusinessRuleException("Number of copies sold must be positive.");
        }
        Book book = getBook(bookId);
        if (!bookDao.adjustStock(bookId, -copies)) {
            throw new BusinessRuleException("Only " + book.getStock() + " copies of \"" + book.getTitle()
                    + "\" are in stock - cannot sell " + copies + ".");
        }
    }

    @Transactional
    public void setPrice(int bookId, BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new BusinessRuleException("The new price must be greater than zero.");
        }
        getBook(bookId);
        bookDao.updatePrice(bookId, newPrice.setScale(2, RoundingMode.HALF_UP));
    }

    /** Applies a percentage discount (1-90 %) to the current price. Returns the new price. */
    @Transactional
    public BigDecimal applyDiscount(int bookId, int percent) {
        if (percent < 1 || percent > 90) {
            throw new BusinessRuleException("Discount must be between 1 and 90 percent.");
        }
        Book book = getBook(bookId);
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(percent)).divide(HUNDRED);
        BigDecimal newPrice = book.getPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        bookDao.updatePrice(bookId, newPrice);
        return newPrice;
    }

    private void checkIsbnFree(String isbn, Integer ignoreBookId) {
        if (bookDao.isbnTaken(isbn, ignoreBookId)) {
            throw new BusinessRuleException("isbn", "Another book already uses ISBN " + isbn + ".");
        }
    }
}
