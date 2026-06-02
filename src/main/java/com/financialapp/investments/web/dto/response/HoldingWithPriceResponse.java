package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HoldingWithPriceResponse {
    private Long id;
    private Long userId;
    private Long bankId;
    private Long bankAccountId;
    private String ticker;
    private String name;
    private String assetType;
    private String quantity;
    private String avgPurchasePrice;
    private String currency;
    private String notifyGainThresholdPct;
    private String notifyLossThresholdPct;
    private LocalDateTime lastGainNotifiedAt;
    private LocalDateTime lastLossNotifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currentPrice;
    private String currentValue;
    private String plAmount;
    private String plPercent;
}
