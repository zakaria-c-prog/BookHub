package edu.njust.bookhub.model;

import java.math.BigDecimal;

/**
 * Query object for the combined book search. Every field is optional; the
 * DAO adds a WHERE condition only for the fields that are filled in, so any
 * combination of filters can be applied at once.
 */
public class BookSearchCriteria {

    public enum SortOrder {
        TITLE("b.title ASC"),
        PRICE_ASC("b.price ASC"),
        PRICE_DESC("b.price DESC"),
        NEWEST("b.publish_year DESC, b.title ASC"),
        STOCK_ASC("b.stock ASC, b.title ASC");

        private final String sql;

        SortOrder(String sql) {
            this.sql = sql;
        }

        public String sql() {
            return sql;
        }
    }

    /** Matches title or ISBN (partial, case-insensitive). */
    private String keyword;
    /** Matches any author's full name (partial). */
    private String author;
    private Integer providerId;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly;
    private SortOrder sort = SortOrder.TITLE;

    public boolean isEmpty() {
        return !hasText(keyword) && !hasText(author) && providerId == null && !hasText(category)
                && minPrice == null && maxPrice == null && !inStockOnly;
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public boolean isInStockOnly() { return inStockOnly; }
    public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }

    public SortOrder getSort() { return sort; }
    public void setSort(SortOrder sort) { this.sort = sort == null ? SortOrder.TITLE : sort; }
}
