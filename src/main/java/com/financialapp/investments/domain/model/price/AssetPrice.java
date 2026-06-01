package com.financialapp.investments.domain.model.price;

import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record AssetPrice(
        AssetPriceId id,
        Ticker ticker,
        AssetType assetType,
        BigDecimal lastPrice,
        String currency,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation,
        LocalDateTime pricedAt,
        LocalDateTime updatedAt
) {
    public AssetPrice {
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(lastPrice, "lastPrice must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
