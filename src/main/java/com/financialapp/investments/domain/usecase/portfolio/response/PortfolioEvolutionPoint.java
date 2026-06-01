package com.financialapp.investments.domain.usecase.portfolio.response;

import com.financialapp.investments.domain.common.model.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PortfolioEvolutionPoint(
        LocalDate date,
        List<Money> totals
) {
    public PortfolioEvolutionPoint {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(totals, "totals must not be null");
        totals = List.copyOf(totals);
    }
}
