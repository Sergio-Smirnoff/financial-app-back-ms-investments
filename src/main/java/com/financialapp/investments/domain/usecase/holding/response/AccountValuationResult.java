package com.financialapp.investments.domain.usecase.holding.response;

import com.financialapp.investments.domain.model.holding.BanksAccountId;

import java.math.BigDecimal;

public record AccountValuationResult(
        BanksAccountId accountId,
        BigDecimal totalValuation,
        String currency,
        long holdingCount
) {}
