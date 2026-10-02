package edu.njust.bookhub.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A title in the catalogue (row of table {@code books} plus its joined data). */
public class Book {

    /** Stock at or below this level is flagged as "low" in the UI. */
    public static final int LOW_STOCK_THRESHOLD = 5;

    private Integer bookId;
    private String title;
    private String isbn;
    private String category;
    private BigDecimal price;
    private int stock;
    private Integer publishYear;
    private String summary;
    private LocalDateTime createdAt;

    private Integer providerId;
    /** Joined from {@code providers.company_name}; null when no provider. */
    private String providerName;

    /** Authors in byline order. */
    private List<Author> authors = new ArrayList<>();

    public boolean isOutOfStock() {
        return stock == 0;
    }

    public boolean isLowStock() {
        return stock > 0 && stock <= LOW_STOCK_THRESHOLD;
    }

    public String getAuthorNames() {
        return authors.stream().map(Author::getFullName).collect(Collectors.joining(", "));
    }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
}
