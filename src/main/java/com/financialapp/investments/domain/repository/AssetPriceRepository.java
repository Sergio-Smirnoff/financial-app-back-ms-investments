package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetPriceRepository {

    AssetPrice save(AssetPrice price);

    Optional<AssetPrice> findByTicker(Ticker ticker);

    List<AssetPrice> findAllByTickerIn(Collection<Ticker> tickers);
}
