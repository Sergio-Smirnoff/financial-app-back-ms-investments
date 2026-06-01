package com.financialapp.investments.domain.usecase.market.response;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.math.BigDecimal;

public record MarketOpportunityResult(
        Ticker ticker,
        Money price,
        BigDecimal variation,
        BigDecimal volume
) {}
