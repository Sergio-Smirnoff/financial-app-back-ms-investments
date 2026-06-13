package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HoldingResponse {
    private Long id;
    private Long userId;
    private String bankNumber;
    private String ticker;
    private String name;
    private String assetType;
    private String quantity;
    private String avgPurchasePrice;
    private String currency;
    private String notifyGainThresholdPct;
    private String notifyLossThresholdPct;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
