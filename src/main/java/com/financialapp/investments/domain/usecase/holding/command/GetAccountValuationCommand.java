package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.model.holding.BanksAccountId;

public record GetAccountValuationCommand(BanksAccountId accountId) {}
