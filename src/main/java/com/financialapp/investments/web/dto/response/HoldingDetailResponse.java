package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class HoldingDetailResponse {
    private Long id;
    private Long userId;
    private Long bankId;
    private Long bankAccountId;
    private String ticker;
    private String name;
    private String assetType;
    private BigDecimal quantity;
    private BigDecimal avgPurchasePrice;
    private String currency;
    private BigDecimal notifyGainThresholdPct;
    private BigDecimal notifyLossThresholdPct;
    private LocalDateTime lastGainNotifiedAt;
    private LocalDateTime lastLossNotifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal plAmount;
    private BigDecimal plPercent;
}
