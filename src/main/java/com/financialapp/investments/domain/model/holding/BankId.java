package com.financialapp.investments.domain.model.holding;

import java.util.Objects;

public record BankId(Long value) {

    public BankId {
        Objects.requireNonNull(value, "bankId must not be null");
    }
}
