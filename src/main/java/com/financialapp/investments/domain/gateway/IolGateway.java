package com.financialapp.investments.domain.gateway;

import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IolGateway {

    Optional<PriceDetail> getPrice(Ticker ticker, AssetType assetType);

    List<HistoricalPricePoint> getHistoricalSeries(Ticker ticker, AssetType assetType,
                                                    LocalDate from, LocalDate to);

    List<MarketQuote> getPanelQuotes(String panel);
}
