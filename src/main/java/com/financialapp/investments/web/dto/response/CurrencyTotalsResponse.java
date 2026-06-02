package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CurrencyTotalsResponse {
    private String currency;
    private String totalValue;
    private String totalCost;
    private String totalPl;
    private String plPercent;
    private List<AllocationBreakdown> breakdown;
}
