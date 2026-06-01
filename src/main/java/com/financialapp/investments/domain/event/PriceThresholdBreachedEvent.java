package com.financialapp.investments.domain.event;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceThresholdBreachedEvent(
        HoldingId holdingId,
        UserId userId,
        Ticker ticker,
        String holdingName,
        Direction direction,
        BigDecimal thresholdPct,
        BigDecimal actualPct,
        Money currentPrice,
        Money avgPurchasePrice,
        LocalDateTime occurredAt
) implements DomainEvent {
}
