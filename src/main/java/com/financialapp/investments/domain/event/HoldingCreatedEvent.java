package com.financialapp.investments.domain.event;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;

import java.time.LocalDateTime;

public record HoldingCreatedEvent(
        HoldingId holdingId,
        UserId userId,
        Ticker ticker,
        AssetType assetType,
        BankNumber bankNumber,
        Cbu fundingCbu,
        HoldingQuantity quantity,
        Money avgPurchasePrice,
        Money totalCost,
        LocalDateTime occurredAt
) implements DomainEvent {}
