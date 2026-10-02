package edu.njust.bookhub.web.form;

import edu.njust.bookhub.model.Author;
import edu.njust.bookhub.model.Book;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Backing object of the add / edit book page, with its validation rules. */
public class BookForm {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "\\d{9}[\\dX]|\\d{13}", message = "ISBN must have 10 or 13 digits (hyphens are ignored)")
    private String isbn;

    @NotBlank(message = "Choose or type a category")
    @Size(max = 60)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 6, fraction = 2, message = "Price may have at most 2 decimals")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    @Min(value = 1450, message = "Year looks too early")
    @Max(value = 2100, message = "Year looks too late")
    private Integer publishYear;

    @Size(max = 2000, message = "Summary must be at most 2000 characters")
    private String summary;

    private Integer providerId;

    private List<Integer> authorIds = new ArrayList<>();

    public static BookForm from(Book b) {
        BookForm f = new BookForm();
        f.title = b.getTitle();
        f.isbn = b.getIsbn();
        f.category = b.getCategory();
        f.price = b.getPrice();
        f.stock = b.getStock();
        f.publishYear = b.getPublishYear();
        f.summary = b.getSummary();
        f.providerId = b.getProviderId();
        f.authorIds = new ArrayList<>(b.getAuthors().stream().map(Author::getAuthorId).toList());
        return f;
    }

    public Book toBook(Integer bookId) {
        Book b = new Book();
        b.setBookId(bookId);
        b.setTitle(title.trim());
        b.setIsbn(isbn);
        b.setCategory(category.trim());
        b.setPrice(price);
        b.setStock(stock);
        b.setPublishYear(publishYear);
        b.setSummary(summary == null || summary.isBlank() ? null : summary.trim());
        b.setProviderId(providerId);
        return b;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    /** Normalises "978-7-300-00001-1" to "9787300000011" before validation. */
    public void setIsbn(String isbn) {
        this.isbn = isbn == null ? null : isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }

    public List<Integer> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Integer> authorIds) { this.authorIds = authorIds == null ? new ArrayList<>() : authorIds; }
}
