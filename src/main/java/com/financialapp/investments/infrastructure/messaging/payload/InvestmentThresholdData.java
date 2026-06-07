package com.financialapp.investments.infrastructure.messaging.payload;

import java.math.BigDecimal;

public record InvestmentThresholdData(
        Long userId,
        Long holdingId,
        String ticker,
        String name,
        String direction,
        BigDecimal thresholdPct,
        BigDecimal actualPct,
        BigDecimal currentPrice,
        BigDecimal avgPurchasePrice,
        String currency
) {
}
