package com.financialapp.investments.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {
    private BigDecimal totalValueArs;
    private BigDecimal totalValueUsd;
    private BigDecimal totalPlArs;
    private BigDecimal totalPlUsd;
    private BigDecimal plPercentArs;
    private BigDecimal plPercentUsd;
    private List<AllocationBreakdown> breakdownArs;
    private List<AllocationBreakdown> breakdownUsd;
}
