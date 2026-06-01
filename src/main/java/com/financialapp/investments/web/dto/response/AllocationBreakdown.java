package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AllocationBreakdown {
    private String assetType;
    private BigDecimal totalValue;
    private BigDecimal percentage;
}
