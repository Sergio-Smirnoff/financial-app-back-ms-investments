package com.financialapp.investments.kafka.event;

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
public class InvestmentThresholdEvent {

    @Builder.Default
    private String eventType = "INVESTMENT_THRESHOLD";
    private Long userId;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long holdingId;
        private String ticker;
        private String name;
        private String direction; // GAIN or LOSS
        private BigDecimal thresholdPct;
        private BigDecimal actualPct;
        private BigDecimal currentPrice;
        private BigDecimal avgPurchasePrice;
        private String currency;
    }
}
