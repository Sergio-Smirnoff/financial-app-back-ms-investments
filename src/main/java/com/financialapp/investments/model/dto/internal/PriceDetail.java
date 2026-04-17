package com.financialapp.investments.model.dto.internal;

import java.math.BigDecimal;

public record PriceDetail(
        BigDecimal lastPrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal volume,
        BigDecimal dailyVariation
) {}
