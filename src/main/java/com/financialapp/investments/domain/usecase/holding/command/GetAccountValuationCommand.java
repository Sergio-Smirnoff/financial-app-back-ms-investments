package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.UserId;

import java.util.Currency;

public record GetAccountValuationCommand(UserId userId, BankNumber bankNumber, Currency currency) {}
