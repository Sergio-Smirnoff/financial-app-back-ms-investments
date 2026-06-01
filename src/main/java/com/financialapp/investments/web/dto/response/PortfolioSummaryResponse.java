package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortfolioSummaryResponse {
    private List<CurrencyTotalsResponse> byCurrency;
}
