package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllocationBreakdown {
    private String assetType;
    private String totalValue;
    private String percentage;
}
