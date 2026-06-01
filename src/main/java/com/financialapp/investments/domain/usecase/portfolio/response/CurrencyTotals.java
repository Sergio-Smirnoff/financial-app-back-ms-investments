package com.financialapp.investments.domain.usecase.portfolio.response;

import com.financialapp.investments.domain.common.model.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public record CurrencyTotals(
        Money totalValue,
        Money totalCost,
        Money totalPl,
        BigDecimal plPercent,
        List<AllocationBreakdownResult> breakdown
) {
    public CurrencyTotals {
        Objects.requireNonNull(totalValue, "totalValue must not be null");
        Objects.requireNonNull(totalCost, "totalCost must not be null");
        Objects.requireNonNull(totalPl, "totalPl must not be null");
        Objects.requireNonNull(plPercent, "plPercent must not be null");
        breakdown = breakdown == null ? List.of() : List.copyOf(breakdown);
        if (!totalValue.currency().equals(totalCost.currency())
                || !totalValue.currency().equals(totalPl.currency())) {
            throw new IllegalArgumentException(
                    "CurrencyTotals: Money fields must share the same currency");
        }
    }

    public Currency currency() {
        return totalValue.currency();
    }
}
