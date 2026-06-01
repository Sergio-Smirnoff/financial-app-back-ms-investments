package com.financialapp.investments.domain.model.price;

import java.math.BigDecimal;
import java.util.Objects;

public record PriceDetail(
        BigDecimal lastPrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation,
        String currency
) {
    public PriceDetail {
        Objects.requireNonNull(lastPrice, "lastPrice must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
