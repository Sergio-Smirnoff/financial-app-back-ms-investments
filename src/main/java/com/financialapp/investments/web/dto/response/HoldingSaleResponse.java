package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HoldingSaleResponse {
    private Long holdingId;
    private String soldQuantity;
    private String remainingQuantity;
    private String proceeds;
    private String bookedAmount;
    private String currency;
    private boolean closed;
}
