package edu.njust.bookhub.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Tells the templates whether a book has illustrated cover art in static/covers
 * ({@code ${@covers.has(book.bookId)}}); books without art get the styled fallback cover.
 */
@Component("covers")
public class Covers {

    public boolean has(Integer bookId) {
        return bookId != null && new ClassPathResource("static/covers/" + bookId + ".svg").exists();
    }
}
