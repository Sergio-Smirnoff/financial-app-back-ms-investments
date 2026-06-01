package com.financialapp.investments.domain.usecase.portfolio.command;

import com.financialapp.investments.domain.common.model.UserId;

public record GetHoldingsWithPricesCommand(UserId userId) {}
