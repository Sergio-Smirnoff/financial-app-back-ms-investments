package com.financialapp.investments.infrastructure.gateway.dto;

import java.math.BigDecimal;

public record IolMarketQuote(
        String ticker,
        BigDecimal price,
        BigDecimal variation
) {}
