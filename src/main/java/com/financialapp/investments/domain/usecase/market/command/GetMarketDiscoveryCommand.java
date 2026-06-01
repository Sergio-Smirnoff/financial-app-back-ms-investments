package com.financialapp.investments.domain.usecase.market.command;

import com.financialapp.investments.domain.common.model.UserId;

public record GetMarketDiscoveryCommand(UserId userId, int limit) {}
