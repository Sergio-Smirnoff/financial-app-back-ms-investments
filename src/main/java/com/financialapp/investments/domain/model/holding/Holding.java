package com.financialapp.investments.domain.model.holding;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.holding.HoldingSaleExceedsQuantityException;
import com.financialapp.investments.domain.model.price.AssetType;

import java.time.LocalDateTime;
import java.util.Objects;

public record Holding(
        HoldingId id,
        UserId userId,
        BankNumber bankNumber,
        Ticker ticker,
        String name,
        AssetType assetType,
        HoldingQuantity quantity,
        Money avgPurchasePrice,
        ThresholdConfig thresholdConfig,
        NotificationTimestamps notificationTimestamps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Holding {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(ticker, "ticker must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(avgPurchasePrice, "avgPurchasePrice must not be null");
        Objects.requireNonNull(notificationTimestamps,
                "notificationTimestamps must not be null — use NotificationTimestamps.empty()");
    }

    public static Holding create(UserId userId, BankNumber bankNumber, Ticker ticker, String name,
                                 AssetType assetType, HoldingQuantity quantity, Money avgPurchasePrice,
                                 ThresholdConfig thresholdConfig) {
        LocalDateTime now = LocalDateTime.now();
        return new Holding(new HoldingId(null), userId, bankNumber, ticker, name, assetType,
                quantity, avgPurchasePrice, thresholdConfig, NotificationTimestamps.empty(), now, now);
    }

    public Holding withNotificationTimestamps(NotificationTimestamps timestamps) {
        return new Holding(id, userId, bankNumber, ticker, name, assetType,
                quantity, avgPurchasePrice, thresholdConfig, timestamps, createdAt, LocalDateTime.now());
    }

    public Money costBasis() {
        return costBasisOf(quantity);
    }

    public Money costBasisOf(HoldingQuantity sold) {
        return avgPurchasePrice.multiply(sold.value());
    }

    public Money saleProceeds(Money quote, HoldingQuantity sold) {
        return new Money(assetType.marketValue(quote.amount(), sold.value()), quote.currency());
    }

    public Money saleProceeds(ManualQuote quote, HoldingQuantity sold) {
        return saleProceeds(new Money(quote.value(), avgPurchasePrice.currency()), sold);
    }

    public boolean isFullySoldBy(HoldingQuantity sold) {
        return sold.value().compareTo(quantity.value()) == 0;
    }

    public Holding afterSelling(HoldingQuantity sold) {
        if (sold.value().compareTo(quantity.value()) > 0) {
            throw new HoldingSaleExceedsQuantityException(sold, quantity);
        }
        return new Holding(id, userId, bankNumber, ticker, name, assetType,
                new HoldingQuantity(quantity.value().subtract(sold.value())), avgPurchasePrice,
                thresholdConfig, notificationTimestamps, createdAt, LocalDateTime.now());
    }

    public Money unitPrice(Money quote) {
        return new Money(assetType.unitPrice(quote.amount()), quote.currency());
    }

    public Money marketValue(Money quote) {
        return saleProceeds(quote, quantity);
    }

    public PositionValuation valuation(Money quote) {
        return new PositionValuation(marketValue(quote), costBasis());
    }

    public PositionValuation valuationAtCost() {
        return new PositionValuation(costBasis(), costBasis());
    }
}
