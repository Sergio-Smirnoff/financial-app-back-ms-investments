package com.financialapp.investments.domain.model.history;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record AssetPriceHistory(
        AssetPriceHistoryId id,
        Ticker ticker,
        AssetType assetType,
        BigDecimal lastPrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation,
        String currency,
        LocalDateTime pricedAt
) {
    public AssetPriceHistory {
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(lastPrice, "lastPrice must not be null");
        Objects.requireNonNull(pricedAt, "pricedAt must not be null");
    }
}
