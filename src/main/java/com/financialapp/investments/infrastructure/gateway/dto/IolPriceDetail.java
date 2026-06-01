package com.financialapp.investments.infrastructure.gateway.dto;

import java.math.BigDecimal;

public record IolPriceDetail(
        BigDecimal lastPrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation
) {}
