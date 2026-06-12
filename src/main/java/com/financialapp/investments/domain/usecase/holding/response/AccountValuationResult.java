package com.financialapp.investments.domain.usecase.holding.response;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;

public record AccountValuationResult(
        BankNumber bankNumber,
        Money totalValuation,
        long holdingCount
) {}
