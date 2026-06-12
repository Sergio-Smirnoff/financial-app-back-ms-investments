package com.financialapp.investments.domain.model.market;

import java.time.LocalDate;

public enum PriceRange {
    D30(30), D90(90), Y1(365), ALL(365 * 5);

    private final int days;

    PriceRange(int days) {
        this.days = days;
    }

    public static PriceRange of(String code) {
        return PriceRange.valueOf(code.toUpperCase());
    }

    public LocalDate from(LocalDate reference) {
        return reference.minusDays(days);
    }

    public LocalDate to(LocalDate reference) {
        return reference;
    }
}
