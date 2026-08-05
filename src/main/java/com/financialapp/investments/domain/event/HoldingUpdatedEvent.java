package com.financialapp.investments.domain.event;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.time.LocalDateTime;

public record HoldingUpdatedEvent(
        HoldingId holdingId,
        UserId userId,
        Ticker ticker,
        BankNumber bankNumber,
        Cbu fundingCbu,
        HoldingQuantity newQuantity,
        HoldingQuantity previousQuantity,
        Money newAvgPurchasePrice,
        Money costDifference,
        LocalDateTime occurredAt
) implements DomainEvent {}
