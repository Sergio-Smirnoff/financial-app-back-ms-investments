package com.financialapp.investments.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationBreakdown {
    private String assetType;
    private BigDecimal totalValue;
    private String currency;
    private BigDecimal percentage;
}
