package edu.njust.bookhub.dao.jdbc;

import edu.njust.bookhub.dao.AuthorDao;
import edu.njust.bookhub.model.Author;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcAuthorDao implements AuthorDao {

    private static final RowMapper<Author> AUTHOR_MAPPER = (rs, rowNum) -> {
        Author a = new Author();
        a.setAuthorId(rs.getInt("author_id"));
        a.setFullName(rs.getString("full_name"));
        a.setNationality(rs.getString("nationality"));
        a.setEmail(rs.getString("email"));
        a.setBiography(rs.getString("biography"));
        return a;
    };

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAuthorDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Author> findAll(String nameFilter) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.*, COUNT(ba.book_id) AS book_count
                FROM authors a
                LEFT JOIN book_authors ba ON ba.author_id = a.author_id""");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (nameFilter != null && !nameFilter.isBlank()) {
            sql.append(" WHERE LOWER(a.full_name) LIKE :name");
            params.addValue("name", "%" + nameFilter.trim().toLowerCase() + "%");
        }
        sql.append(" GROUP BY a.author_id, a.full_name, a.nationality, a.email, a.biography")
           .append(" ORDER BY a.full_name");
        return jdbc.query(sql.toString(), params, (rs, i) -> {
            Author a = AUTHOR_MAPPER.mapRow(rs, i);
            a.setBookCount(rs.getInt("book_count"));
            return a;
        });
    }

    @Override
    public Optional<Author> findById(int authorId) {
        return jdbc.query("SELECT * FROM authors WHERE author_id = :id",
                new MapSqlParameterSource("id", authorId), AUTHOR_MAPPER).stream().findFirst();
    }

    @Override
    public Map<Integer, List<Author>> findByBookIds(Collection<Integer> bookIds) {
        Map<Integer, List<Author>> result = new HashMap<>();
        if (bookIds.isEmpty()) {
            return result;
        }
        jdbc.query("""
                        SELECT ba.book_id, a.*
                        FROM book_authors ba JOIN authors a ON a.author_id = ba.author_id
                        WHERE ba.book_id IN (:ids)
                        ORDER BY ba.book_id, ba.author_order""",
                new MapSqlParameterSource("ids", bookIds),
                rs -> {
                    result.computeIfAbsent(rs.getInt("book_id"), k -> new ArrayList<>())
                            .add(AUTHOR_MAPPER.mapRow(rs, 0));
                });
        return result;
    }

    @Override
    public int count() {
        Integer n = jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM authors", Integer.class);
        return n == null ? 0 : n;
    }

    @Override
    public int insert(Author author) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update("""
                        INSERT INTO authors (full_name, nationality, email, biography)
                        VALUES (:fullName, :nationality, :email, :biography)""",
                params(author), keys, new String[]{"author_id"});
        int id = keys.getKey().intValue();
        author.setAuthorId(id);
        return id;
    }

    @Override
    public void update(Author author) {
        jdbc.update("""
                        UPDATE authors SET full_name = :fullName, nationality = :nationality,
                               email = :email, biography = :biography
                        WHERE author_id = :id""",
                params(author).addValue("id", author.getAuthorId()));
    }

    @Override
    public boolean delete(int authorId) {
        return jdbc.update("DELETE FROM authors WHERE author_id = :id",
                new MapSqlParameterSource("id", authorId)) == 1;
    }

    private static MapSqlParameterSource params(Author a) {
        return new MapSqlParameterSource()
                .addValue("fullName", a.getFullName())
                .addValue("nationality", a.getNationality())
                .addValue("email", a.getEmail())
                .addValue("biography", a.getBiography());
    }
}
