package com.financialapp.investments.infrastructure.messaging.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentThresholdPayload {

    @Builder.Default
    private String eventType = "INVESTMENT_THRESHOLD";
    private Long userId;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Data data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long holdingId;
        private String ticker;
        private String name;
        private String direction;
        private BigDecimal thresholdPct;
        private BigDecimal actualPct;
        private BigDecimal currentPrice;
        private BigDecimal avgPurchasePrice;
        private String currency;
    }
}
