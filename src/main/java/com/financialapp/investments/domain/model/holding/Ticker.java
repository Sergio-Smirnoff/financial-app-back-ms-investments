package com.financialapp.investments.domain.model.holding;

import java.util.Objects;

public record Ticker(String value) {

    public Ticker {
        Objects.requireNonNull(value, "ticker must not be null");
        value = value.toUpperCase();
        if (!value.matches("[A-Z0-9.]{1,20}")) {
            throw new IllegalArgumentException(
                    "Invalid ticker format: '" + value + "' — must be 1-20 uppercase alphanumeric chars");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
