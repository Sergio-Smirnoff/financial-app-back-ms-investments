package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.time.LocalDateTime;
import java.util.List;

public interface AssetPriceHistoryRepository {

    AssetPriceHistory save(AssetPriceHistory history);

    List<AssetPriceHistory> findByTicker(Ticker ticker);

    List<AssetPriceHistory> findByTickerAndPricedAtBetween(Ticker ticker,
                                                            LocalDateTime from,
                                                            LocalDateTime to);

    long countByTickerAndPricedAtBetween(Ticker ticker, LocalDateTime from, LocalDateTime to);

    boolean existsByTickerAndPricedAt(Ticker ticker, LocalDateTime pricedAt);
}
