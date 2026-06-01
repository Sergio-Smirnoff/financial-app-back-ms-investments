package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.investments.infrastructure.gateway.IolApiClient;
import com.financialapp.investments.infrastructure.gateway.dto.IolHistoricalPricePoint;
import com.financialapp.investments.infrastructure.gateway.dto.IolMarketQuote;
import com.financialapp.investments.infrastructure.gateway.dto.IolPriceDetail;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.gateway.IolGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IolGatewayImpl implements IolGateway {

    private static final String DEFAULT_CURRENCY = "ARS";

    private final IolApiClient iolApiClient;

    @Override
    public Optional<PriceDetail> getPrice(Ticker ticker, AssetType assetType) {
        try {
            return iolApiClient.getPrice(ticker.value(), assetType)
                    .map(this::toDomainPriceDetail);
        } catch (RuntimeException e) {
            throw new InfrastructureException(
                    "IOL price fetch failed for ticker: " + ticker.value(), e);
        }
    }

    @Override
    public List<HistoricalPricePoint> getHistoricalSeries(
            Ticker ticker, AssetType assetType, LocalDate from, LocalDate to) {
        try {
            return iolApiClient.getHistoricalSeries(ticker.value(), assetType, from, to)
                    .stream()
                    .map(this::toDomainHistoricalPoint)
                    .toList();
        } catch (RuntimeException e) {
            throw new InfrastructureException(
                    "IOL historical series fetch failed for ticker: " + ticker.value(), e);
        }
    }

    @Override
    public List<MarketQuote> getPanelQuotes(String panel) {
        try {
            return iolApiClient.getPanelQuotes(panel)
                    .stream()
                    .map(this::toDomainMarketQuote)
                    .toList();
        } catch (RuntimeException e) {
            throw new InfrastructureException(
                    "IOL panel quotes fetch failed for panel: " + panel, e);
        }
    }

    private PriceDetail toDomainPriceDetail(IolPriceDetail old) {
        return new PriceDetail(
                old.lastPrice(),
                old.openPrice(),
                old.highPrice(),
                old.lowPrice(),
                old.volume(),
                old.dailyVariation(),
                DEFAULT_CURRENCY
        );
    }

    private HistoricalPricePoint toDomainHistoricalPoint(IolHistoricalPricePoint old) {
        return new HistoricalPricePoint(
                old.detail().lastPrice(),
                old.detail().openPrice(),
                old.detail().highPrice(),
                old.detail().lowPrice(),
                old.detail().volume(),
                old.detail().dailyVariation(),
                DEFAULT_CURRENCY,
                old.pricedAt()
        );
    }

    private MarketQuote toDomainMarketQuote(IolMarketQuote old) {
        return new MarketQuote(
                new Ticker(old.ticker()),
                Money.of(old.price() != null ? old.price() : java.math.BigDecimal.ZERO, DEFAULT_CURRENCY),
                old.variation(),
                null,
                LocalDateTime.now()
        );
    }
}
