package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CurrencyTotalsResponse {
    private String currency;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalPl;
    private BigDecimal plPercent;
    private List<AllocationBreakdown> breakdown;
}
