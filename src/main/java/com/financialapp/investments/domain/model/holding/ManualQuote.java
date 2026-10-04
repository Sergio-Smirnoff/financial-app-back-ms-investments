package com.financialapp.investments.domain.model.holding;

import java.math.BigDecimal;
import java.util.Objects;

public record ManualQuote(BigDecimal value) {

    private static final int MAX_DECIMALS = 6;

    public ManualQuote {
        Objects.requireNonNull(value, "manual quote must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Manual quote must be greater than zero");
        }
        if (value.scale() > MAX_DECIMALS) {
            throw new IllegalArgumentException("Manual quote allows at most " + MAX_DECIMALS + " decimals");
        }
    }
}
