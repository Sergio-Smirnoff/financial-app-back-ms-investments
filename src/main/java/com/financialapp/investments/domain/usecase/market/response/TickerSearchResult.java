package com.financialapp.investments.domain.usecase.market.response;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;
import java.util.Objects;

public record TickerSearchResult(
        Ticker ticker,
        Money price,
        BigDecimal variation
) {
    public TickerSearchResult {
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(price, "price must not be null");
    }
}
