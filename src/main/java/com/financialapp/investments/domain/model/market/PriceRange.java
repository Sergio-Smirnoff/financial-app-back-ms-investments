package com.financialapp.investments.domain.model.market;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum PriceRange {
    D30(30, ChronoUnit.DAYS),
    D90(90, ChronoUnit.DAYS),
    Y1(1, ChronoUnit.YEARS),
    ALL(5, ChronoUnit.YEARS);

    private final long amount;
    private final ChronoUnit unit;

    PriceRange(long amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public static PriceRange of(String code) {
        return PriceRange.valueOf(code.toUpperCase());
    }

    public LocalDate from(LocalDate reference) {
        return reference.minus(amount, unit);
    }

    public LocalDate to(LocalDate reference) {
        return reference;
    }
}
