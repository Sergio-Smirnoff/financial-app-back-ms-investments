package com.financialapp.investments.domain.usecase.portfolio.response;

import com.financialapp.investments.domain.model.holding.Holding;

import java.math.BigDecimal;

public record HoldingWithPriceResult(
        Holding holding,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal plAmount,
        BigDecimal plPercent
) {}
