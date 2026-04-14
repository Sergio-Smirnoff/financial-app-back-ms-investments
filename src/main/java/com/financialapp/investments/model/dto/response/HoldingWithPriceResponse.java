package com.financialapp.investments.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingWithPriceResponse {
    private Long id;
    private Long userId;
    private String ticker;
    private String name;
    private String assetType;
    private BigDecimal quantity;
    private BigDecimal avgPurchasePrice;
    private String currency;
    private BigDecimal notifyGainThresholdPct;
    private BigDecimal notifyLossThresholdPct;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal plAmount;
    private BigDecimal plPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
