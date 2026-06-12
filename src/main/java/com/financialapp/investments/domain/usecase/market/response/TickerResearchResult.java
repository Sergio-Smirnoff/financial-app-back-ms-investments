package com.financialapp.investments.domain.usecase.market.response;

import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.PriceDetail;

import java.util.List;
import java.util.Optional;

public record TickerResearchResult(
        Ticker ticker,
        Optional<PriceDetail> currentQuote,
        List<HistoricalPricePoint> series
) {}
