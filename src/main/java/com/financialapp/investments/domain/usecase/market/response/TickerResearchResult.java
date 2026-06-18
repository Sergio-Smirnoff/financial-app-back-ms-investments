package com.financialapp.investments.domain.usecase.market.response;

import com.financialapp.investments.domain.model.history.PriceSeries;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.PriceDetail;

import java.util.Objects;
import java.util.Optional;

public record TickerResearchResult(
        Ticker ticker,
        Optional<PriceDetail> currentQuote,
        PriceSeries series
) {
    public TickerResearchResult {
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(series, "series must not be null");
    }
}
