package edu.njust.bookhub.model;

import java.math.BigDecimal;
import java.util.List;

/** Figures shown on the dashboard. */
public record CatalogStats(
        int bookCount,
        int authorCount,
        int providerCount,
        int unitsInStock,
        BigDecimal inventoryValue,
        List<CategoryCount> categories) {

    public record CategoryCount(String category, int titles, int units) {
    }
}
