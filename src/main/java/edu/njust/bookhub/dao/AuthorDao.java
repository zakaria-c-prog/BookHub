package edu.njust.bookhub.dao;

import edu.njust.bookhub.model.Author;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Data access for the {@code authors} table. */
public interface AuthorDao {

    /** All authors (optionally filtered by partial name), each with its book count. */
    List<Author> findAll(String nameFilter);

    Optional<Author> findById(int authorId);

    /** Authors of each of the given books, keyed by book id, in byline order. */
    Map<Integer, List<Author>> findByBookIds(Collection<Integer> bookIds);

    int count();

    int insert(Author author);

    void update(Author author);

    boolean delete(int authorId);
}
