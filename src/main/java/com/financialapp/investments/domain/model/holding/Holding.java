package com.financialapp.investments.domain.model.holding;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
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
        Objects.requireNonNull(notificationTimestamps, "notificationTimestamps must not be null — use NotificationTimestamps.empty()");
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
}
