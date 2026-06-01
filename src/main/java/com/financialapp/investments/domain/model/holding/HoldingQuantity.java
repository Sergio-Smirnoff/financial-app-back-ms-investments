package com.financialapp.investments.domain.model.holding;

import com.financialapp.investments.domain.exception.holding.HoldingQuantityNonPositiveException;

import java.math.BigDecimal;
import java.util.Objects;

public record HoldingQuantity(BigDecimal value) {

    public HoldingQuantity {
        Objects.requireNonNull(value, "quantity must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HoldingQuantityNonPositiveException();
        }
    }
}
