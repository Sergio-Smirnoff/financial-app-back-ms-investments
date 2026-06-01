package com.financialapp.investments.domain.model.history;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record HistoricalPricePoint(
        BigDecimal lastPrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation,
        String currency,
        LocalDateTime pricedAt
) {
    public HistoricalPricePoint {
        Objects.requireNonNull(lastPrice, "lastPrice must not be null");
        Objects.requireNonNull(pricedAt, "pricedAt must not be null");
    }
}
