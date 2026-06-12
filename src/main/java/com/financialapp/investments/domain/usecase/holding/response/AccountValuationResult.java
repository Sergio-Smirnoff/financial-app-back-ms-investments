package com.financialapp.investments.domain.usecase.holding.response;

import com.financialapp.investments.domain.common.model.BankNumber;

import java.math.BigDecimal;

public record AccountValuationResult(
        BankNumber bankNumber,
        BigDecimal totalValuation,
        String currency,
        long holdingCount
) {}
