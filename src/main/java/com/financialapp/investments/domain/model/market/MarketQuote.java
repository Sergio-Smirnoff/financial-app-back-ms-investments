package com.financialapp.investments.domain.model.market;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record MarketQuote(
        Ticker ticker,
        Money price,
        BigDecimal variation,
        BigDecimal volume,
        LocalDateTime updatedAt
) {
    public MarketQuote {
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(price, "price must not be null");
    }
}
