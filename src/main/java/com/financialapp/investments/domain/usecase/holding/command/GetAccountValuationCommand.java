package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.Cbu;

public record GetAccountValuationCommand(Cbu accountCbu) {}
