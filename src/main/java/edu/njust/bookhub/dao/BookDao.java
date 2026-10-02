package edu.njust.bookhub.dao;

import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.BookSearchCriteria;
import edu.njust.bookhub.model.CatalogStats.CategoryCount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Data access for the {@code books} and {@code book_authors} tables. */
public interface BookDao {

    // ----- lookups -----
    List<Book> search(BookSearchCriteria criteria);

    Optional<Book> findById(int bookId);

    List<Book> findByAuthor(int authorId);

    List<Book> findByProvider(int providerId);

    List<Book> findLowStock(int threshold);

    List<Book> findRecentlyAdded(int limit);

    List<String> findCategories();

    List<CategoryCount> countByCategory();

    boolean isbnTaken(String isbn, Integer ignoreBookId);

    int count();

    // ----- updates -----
    int insert(Book book, List<Integer> authorIds);

    void update(Book book, List<Integer> authorIds);

    /** Adds {@code delta} (may be negative) to stock; returns false if stock would go below zero. */
    boolean adjustStock(int bookId, int delta);

    void updatePrice(int bookId, BigDecimal newPrice);

    boolean delete(int bookId);
}
