package edu.njust.bookhub;

import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.BookSearchCriteria;
import edu.njust.bookhub.model.Author;
import edu.njust.bookhub.service.AuthorService;
import edu.njust.bookhub.service.BusinessRuleException;
import edu.njust.bookhub.service.CatalogService;
import edu.njust.bookhub.service.NotFoundException;
import edu.njust.bookhub.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs the service + DAO layers against the H2 "demo" database seeded from data.sql. */
@SpringBootTest
@ActiveProfiles("demo")
@Transactional // each test is rolled back
class CatalogServiceTest {

    @Autowired
    CatalogService catalog;

    @Autowired
    ProviderService providers;

    @Autowired
    AuthorService authors;

    @Test
    void authorListFiltersByNameAndCountsBooks() {
        assertThat(authors.list(null)).hasSize(6);
        List<Author> found = authors.list("wei");
        assertThat(found).extracting(Author::getFullName).containsExactly("Wei Jianguo");
        assertThat(found.get(0).getBookCount()).isEqualTo(3);
    }

    @Test
    void searchByKeywordMatchesTitleCaseInsensitively() {
        BookSearchCriteria c = new BookSearchCriteria();
        c.setKeyword("DESIGN");
        assertThat(catalog.search(c)).extracting(Book::getTitle)
                .contains("People First: Human-Centred Design", "Design Systems Handbook", "Designing Data-Heavy Services");
    }

    @Test
    void searchByAuthorAndPriceRangeCombinesFilters() {
        BookSearchCriteria c = new BookSearchCriteria();
        c.setAuthor("lars");
        c.setMaxPrice(new BigDecimal("80"));
        List<Book> result = catalog.search(c);
        assertThat(result).extracting(Book::getTitle).containsExactly("Testing Distributed Systems");
        assertThat(result.get(0).getAuthors()).hasSize(2);
    }

    @Test
    void searchByProviderReturnsOnlyThatProvidersBooks() {
        BookSearchCriteria c = new BookSearchCriteria();
        c.setProviderId(3);
        assertThat(catalog.search(c)).isNotEmpty().allMatch(b -> b.getProviderId() == 3);
    }

    @Test
    void addBookRejectsDuplicateIsbn() {
        Book b = new Book();
        b.setTitle("Copy");
        b.setIsbn("9787300000011");
        b.setCategory("Fiction");
        b.setPrice(BigDecimal.TEN);
        assertThatThrownBy(() -> catalog.addBook(b, List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    void addBookStoresAuthorsInOrder() {
        Book b = new Book();
        b.setTitle("Brand New Book");
        b.setIsbn("9787300009999");
        b.setCategory("Science");
        b.setPrice(new BigDecimal("30.00"));
        b.setStock(5);
        int id = catalog.addBook(b, List.of(3, 1));
        assertThat(catalog.getBook(id).getAuthors()).extracting(a -> a.getAuthorId()).containsExactly(3, 1);
    }

    @Test
    void cannotSellMoreThanInStock() {
        // book 9 starts with 2 copies
        catalog.recordSale(9, 2);
        assertThat(catalog.getBook(9).getStock()).isZero();
        assertThatThrownBy(() -> catalog.recordSale(9, 1)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void discountReducesPrice() {
        // book 1 costs 68.00; 25% off -> 51.00
        assertThat(catalog.applyDiscount(1, 25)).isEqualByComparingTo("51.00");
        assertThat(catalog.getBook(1).getPrice()).isEqualByComparingTo("51.00");
    }

    @Test
    void deletingProviderKeepsItsBooks() {
        providers.delete(1);
        Book b = catalog.getBook(1);
        assertThat(b.getProviderId()).isNull();
    }

    @Test
    void missingBookRaisesNotFound() {
        assertThatThrownBy(() -> catalog.getBook(9999)).isInstanceOf(NotFoundException.class);
    }
}
