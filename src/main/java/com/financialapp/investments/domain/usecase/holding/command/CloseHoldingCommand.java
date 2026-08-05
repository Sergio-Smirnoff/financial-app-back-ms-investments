package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;

public record CloseHoldingCommand(
        UserId userId,
        HoldingId holdingId,
        Cbu destinationCbu
) {}
