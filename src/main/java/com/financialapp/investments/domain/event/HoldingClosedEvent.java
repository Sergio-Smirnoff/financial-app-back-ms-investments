package com.financialapp.investments.domain.event;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.time.LocalDateTime;

public record HoldingClosedEvent(
        HoldingId holdingId,
        UserId userId,
        Ticker ticker,
        BanksAccountId bankAccountId,
        BanksAccountId depositAccountId,
        Money proceedsAmount,
        LocalDateTime occurredAt
) implements DomainEvent {}
