package com.financialapp.investments.domain.model.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketIndex(
        String code,
        BigDecimal value,
        BigDecimal variation,
        LocalDateTime updatedAt
) {
    public MarketIndex {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("MarketIndex code must not be blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("MarketIndex value must not be null");
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
