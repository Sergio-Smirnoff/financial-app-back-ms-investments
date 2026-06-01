package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;

public record GetHoldingDetailCommand(UserId userId, HoldingId holdingId) {}
