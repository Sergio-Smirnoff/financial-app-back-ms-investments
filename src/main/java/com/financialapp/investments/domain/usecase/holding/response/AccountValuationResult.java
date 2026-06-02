package com.financialapp.investments.domain.usecase.holding.response;

import com.financialapp.investments.domain.common.model.Cbu;

import java.math.BigDecimal;

public record AccountValuationResult(
        Cbu accountCbu,
        BigDecimal totalValuation,
        String currency,
        long holdingCount
) {}
