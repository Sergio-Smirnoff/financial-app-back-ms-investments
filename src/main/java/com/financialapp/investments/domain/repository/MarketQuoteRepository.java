package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;

import java.util.List;
import java.util.Optional;

public interface MarketQuoteRepository {

    void saveAll(List<MarketQuote> quotes);

    List<MarketQuote> findAll();

    Optional<MarketQuote> findByTicker(Ticker ticker);

    List<MarketQuote> search(String query);
}
