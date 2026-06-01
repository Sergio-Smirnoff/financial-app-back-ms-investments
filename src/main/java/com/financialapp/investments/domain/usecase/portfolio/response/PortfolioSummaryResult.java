package com.financialapp.investments.domain.usecase.portfolio.response;

import java.util.List;
import java.util.Objects;

public record PortfolioSummaryResult(List<CurrencyTotals> byCurrency) {

    public PortfolioSummaryResult {
        Objects.requireNonNull(byCurrency, "byCurrency must not be null");
        byCurrency = List.copyOf(byCurrency);
    }
}
