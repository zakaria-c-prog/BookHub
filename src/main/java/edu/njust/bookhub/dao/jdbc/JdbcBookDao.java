package edu.njust.bookhub.dao.jdbc;

import edu.njust.bookhub.dao.AuthorDao;
import edu.njust.bookhub.dao.BookDao;
import edu.njust.bookhub.model.Author;
import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.BookSearchCriteria;
import edu.njust.bookhub.model.CatalogStats.CategoryCount;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcBookDao implements BookDao {

    /** Every book query starts from this SELECT so the row mapper always sees the same columns. */
    private static final String SELECT_BOOK = """
            SELECT b.book_id, b.title, b.isbn, b.category, b.price, b.stock,
                   b.publish_year, b.summary, b.created_at, b.provider_id,
                   p.company_name AS provider_name
            FROM books b
            LEFT JOIN providers p ON p.provider_id = b.provider_id
            """;

    private static final RowMapper<Book> BOOK_MAPPER = (rs, rowNum) -> {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setIsbn(rs.getString("isbn"));
        b.setCategory(rs.getString("category"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setStock(rs.getInt("stock"));
        b.setPublishYear((Integer) rs.getObject("publish_year", Integer.class));
        b.setSummary(rs.getString("summary"));
        Timestamp created = rs.getTimestamp("created_at");
        b.setCreatedAt(created == null ? null : created.toLocalDateTime());
        b.setProviderId((Integer) rs.getObject("provider_id", Integer.class));
        b.setProviderName(rs.getString("provider_name"));
        return b;
    };

    private final NamedParameterJdbcTemplate jdbc;
    private final AuthorDao authorDao;

    public JdbcBookDao(NamedParameterJdbcTemplate jdbc, AuthorDao authorDao) {
        this.jdbc = jdbc;
        this.authorDao = authorDao;
    }

    // ------------------------------------------------------------------ lookups

    @Override
    public List<Book> search(BookSearchCriteria c) {
        StringBuilder sql = new StringBuilder(SELECT_BOOK).append(" WHERE 1 = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (BookSearchCriteria.hasText(c.getKeyword())) {
            sql.append(" AND (LOWER(b.title) LIKE :kw OR b.isbn LIKE :kw)");
            params.addValue("kw", like(c.getKeyword()));
        }
        if (BookSearchCriteria.hasText(c.getAuthor())) {
            sql.append(" AND EXISTS (SELECT 1 FROM book_authors ba")
               .append(" JOIN authors a ON a.author_id = ba.author_id")
               .append(" WHERE ba.book_id = b.book_id AND LOWER(a.full_name) LIKE :author)");
            params.addValue("author", like(c.getAuthor()));
        }
        if (c.getProviderId() != null) {
            sql.append(" AND b.provider_id = :providerId");
            params.addValue("providerId", c.getProviderId());
        }
        if (BookSearchCriteria.hasText(c.getCategory())) {
            sql.append(" AND b.category = :category");
            params.addValue("category", c.getCategory());
        }
        if (c.getMinPrice() != null) {
            sql.append(" AND b.price >= :minPrice");
            params.addValue("minPrice", c.getMinPrice());
        }
        if (c.getMaxPrice() != null) {
            sql.append(" AND b.price <= :maxPrice");
            params.addValue("maxPrice", c.getMaxPrice());
        }
        if (c.isInStockOnly()) {
            sql.append(" AND b.stock > 0");
        }
        // Sort column comes from a fixed enum, never from user text, so concatenation is safe
        sql.append(" ORDER BY ").append(c.getSort().sql());

        return withAuthors(jdbc.query(sql.toString(), params, BOOK_MAPPER));
    }

    @Override
    public Optional<Book> findById(int bookId) {
        List<Book> rows = jdbc.query(SELECT_BOOK + " WHERE b.book_id = :id",
                new MapSqlParameterSource("id", bookId), BOOK_MAPPER);
        return withAuthors(rows).stream().findFirst();
    }

    @Override
    public List<Book> findByAuthor(int authorId) {
        String sql = SELECT_BOOK + """
                 JOIN book_authors ba ON ba.book_id = b.book_id
                 WHERE ba.author_id = :authorId
                 ORDER BY b.publish_year DESC, b.title""";
        return withAuthors(jdbc.query(sql, new MapSqlParameterSource("authorId", authorId), BOOK_MAPPER));
    }

    @Override
    public List<Book> findByProvider(int providerId) {
        String sql = SELECT_BOOK + " WHERE b.provider_id = :providerId ORDER BY b.title";
        return withAuthors(jdbc.query(sql, new MapSqlParameterSource("providerId", providerId), BOOK_MAPPER));
    }

    @Override
    public List<Book> findLowStock(int threshold) {
        String sql = SELECT_BOOK + " WHERE b.stock <= :threshold ORDER BY b.stock, b.title";
        return withAuthors(jdbc.query(sql, new MapSqlParameterSource("threshold", threshold), BOOK_MAPPER));
    }

    @Override
    public List<Book> findRecentlyAdded(int limit) {
        String sql = SELECT_BOOK + " ORDER BY b.created_at DESC, b.book_id DESC LIMIT :limit";
        return withAuthors(jdbc.query(sql, new MapSqlParameterSource("limit", limit), BOOK_MAPPER));
    }

    @Override
    public List<String> findCategories() {
        return jdbc.getJdbcTemplate().queryForList(
                "SELECT DISTINCT category FROM books ORDER BY category", String.class);
    }

    @Override
    public List<CategoryCount> countByCategory() {
        return jdbc.getJdbcTemplate().query("""
                        SELECT category, COUNT(*) AS titles, COALESCE(SUM(stock), 0) AS units
                        FROM books GROUP BY category ORDER BY titles DESC, category""",
                (rs, i) -> new CategoryCount(rs.getString("category"), rs.getInt("titles"), rs.getInt("units")));
    }

    @Override
    public boolean isbnTaken(String isbn, Integer ignoreBookId) {
        MapSqlParameterSource params = new MapSqlParameterSource("isbn", isbn)
                .addValue("ignore", ignoreBookId == null ? -1 : ignoreBookId);
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM books WHERE isbn = :isbn AND book_id <> :ignore", params, Integer.class);
        return n != null && n > 0;
    }

    @Override
    public int count() {
        Integer n = jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        return n == null ? 0 : n;
    }

    // ------------------------------------------------------------------ updates

    @Override
    public int insert(Book book, List<Integer> authorIds) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update("""
                        INSERT INTO books (title, isbn, category, price, stock, publish_year, summary, provider_id)
                        VALUES (:title, :isbn, :category, :price, :stock, :publishYear, :summary, :providerId)""",
                bookParams(book), keys, new String[]{"book_id"});
        int id = keys.getKey().intValue();
        book.setBookId(id);
        replaceAuthors(id, authorIds);
        return id;
    }

    @Override
    public void update(Book book, List<Integer> authorIds) {
        jdbc.update("""
                        UPDATE books SET title = :title, isbn = :isbn, category = :category, price = :price,
                               stock = :stock, publish_year = :publishYear, summary = :summary,
                               provider_id = :providerId
                        WHERE book_id = :bookId""",
                bookParams(book).addValue("bookId", book.getBookId()));
        replaceAuthors(book.getBookId(), authorIds);
    }

    @Override
    public boolean adjustStock(int bookId, int delta) {
        // The guard in the WHERE clause makes the check-and-update atomic
        int rows = jdbc.update("""
                        UPDATE books SET stock = stock + :delta
                        WHERE book_id = :id AND stock + :delta >= 0""",
                new MapSqlParameterSource("id", bookId).addValue("delta", delta));
        return rows == 1;
    }

    @Override
    public void updatePrice(int bookId, BigDecimal newPrice) {
        jdbc.update("UPDATE books SET price = :price WHERE book_id = :id",
                new MapSqlParameterSource("id", bookId).addValue("price", newPrice));
    }

    @Override
    public boolean delete(int bookId) {
        // book_authors rows go with it (ON DELETE CASCADE)
        return jdbc.update("DELETE FROM books WHERE book_id = :id",
                new MapSqlParameterSource("id", bookId)) == 1;
    }

    // ------------------------------------------------------------------ helpers

    private void replaceAuthors(int bookId, List<Integer> authorIds) {
        jdbc.update("DELETE FROM book_authors WHERE book_id = :id", new MapSqlParameterSource("id", bookId));
        if (authorIds == null || authorIds.isEmpty()) {
            return;
        }
        List<MapSqlParameterSource> batch = new ArrayList<>();
        int order = 1;
        for (Integer authorId : authorIds) {
            batch.add(new MapSqlParameterSource("bookId", bookId)
                    .addValue("authorId", authorId)
                    .addValue("order", order++));
        }
        jdbc.batchUpdate("INSERT INTO book_authors (book_id, author_id, author_order) VALUES (:bookId, :authorId, :order)",
                batch.toArray(new MapSqlParameterSource[0]));
    }

    /** Loads the authors of all given books with a single extra query (avoids N+1 selects). */
    private List<Book> withAuthors(List<Book> books) {
        if (books.isEmpty()) {
            return books;
        }
        Map<Integer, List<Author>> byBook = authorDao.findByBookIds(books.stream().map(Book::getBookId).toList());
        for (Book b : books) {
            b.setAuthors(byBook.getOrDefault(b.getBookId(), new ArrayList<>()));
        }
        return books;
    }

    private static MapSqlParameterSource bookParams(Book b) {
        return new MapSqlParameterSource()
                .addValue("title", b.getTitle())
                .addValue("isbn", b.getIsbn())
                .addValue("category", b.getCategory())
                .addValue("price", b.getPrice())
                .addValue("stock", b.getStock())
                .addValue("publishYear", b.getPublishYear())
                .addValue("summary", b.getSummary())
                .addValue("providerId", b.getProviderId());
    }

    private static String like(String text) {
        return "%" + text.trim().toLowerCase() + "%";
    }
}
