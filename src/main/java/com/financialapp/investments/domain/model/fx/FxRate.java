package com.financialapp.investments.domain.model.fx;

import com.financialapp.investments.domain.exception.InvalidFxRateException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FxRate(
        FxRateId id,
        LocalDate date,
        FxView view,
        BigDecimal buy,
        BigDecimal sell,
        FxRateSource source,
        LocalDateTime createdAt
) {
    public FxRate {
        if (date == null) {
            throw new InvalidFxRateException("date must not be null");
        }
        if (view == null) {
            throw new InvalidFxRateException("view must not be null");
        }
        if (source == null) {
            throw new InvalidFxRateException("source must not be null");
        }
        if (buy == null || buy.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFxRateException("buy must be positive: " + buy);
        }
        if (sell == null || sell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFxRateException("sell must be positive: " + sell);
        }
        if (sell.compareTo(buy) < 0) {
            throw new InvalidFxRateException("sell must be greater than or equal to buy: buy=" + buy + ", sell=" + sell);
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
