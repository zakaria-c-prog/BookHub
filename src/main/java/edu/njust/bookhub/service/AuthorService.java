package edu.njust.bookhub.service;

import edu.njust.bookhub.dao.AuthorDao;
import edu.njust.bookhub.model.Author;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorDao authorDao;

    public AuthorService(AuthorDao authorDao) {
        this.authorDao = authorDao;
    }

    public List<Author> list(String nameFilter) {
        return authorDao.findAll(nameFilter);
    }

    public Author get(int authorId) {
        return authorDao.findById(authorId).orElseThrow(() -> new NotFoundException("Author", authorId));
    }

    @Transactional
    public int create(Author author) {
        return authorDao.insert(author);
    }

    @Transactional
    public void update(Author author) {
        get(author.getAuthorId());
        authorDao.update(author);
    }

    /** Removes the author; their books stay in the catalogue with the author unlinked. */
    @Transactional
    public void delete(int authorId) {
        if (!authorDao.delete(authorId)) {
            throw new NotFoundException("Author", authorId);
        }
    }
}
