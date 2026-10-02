package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;

import java.math.BigDecimal;

public record SellHoldingCommand(
        UserId userId,
        HoldingId holdingId,
        HoldingQuantity quantity,
        BigDecimal manualQuote,
        Cbu destinationCbu
) {}
