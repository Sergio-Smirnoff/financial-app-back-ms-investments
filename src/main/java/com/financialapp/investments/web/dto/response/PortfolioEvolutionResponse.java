package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PortfolioEvolutionResponse {
    private LocalDate date;
    private List<CurrencyTotalsByDay> totals;
}
