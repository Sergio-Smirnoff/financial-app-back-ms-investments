package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.web.dto.response.AllocationBreakdown;
import com.financialapp.investments.web.dto.response.CurrencyTotalsByDay;
import com.financialapp.investments.web.dto.response.CurrencyTotalsResponse;
import com.financialapp.investments.web.dto.response.PortfolioEvolutionResponse;
import com.financialapp.investments.web.dto.response.PortfolioSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PortfolioWebMapper {

    public PortfolioSummaryResponse toResponse(PortfolioSummaryResult result) {
        return PortfolioSummaryResponse.builder()
                .byCurrency(result.byCurrency().stream()
                        .map(this::toCurrencyTotalsResponse)
                        .toList())
                .build();
    }

    public PortfolioEvolutionResponse toEvolutionResponse(PortfolioEvolutionPoint point) {
        return PortfolioEvolutionResponse.builder()
                .date(point.date())
                .totals(point.totals().stream()
                        .map(this::toCurrencyTotalsByDay)
                        .toList())
                .build();
    }

    private CurrencyTotalsResponse toCurrencyTotalsResponse(CurrencyTotals totals) {
        return CurrencyTotalsResponse.builder()
                .currency(totals.currency().getCurrencyCode())
                .totalValue(totals.totalValue().amount())
                .totalCost(totals.totalCost().amount())
                .totalPl(totals.totalPl().amount())
                .plPercent(totals.plPercent())
                .breakdown(toBreakdowns(totals.breakdown()))
                .build();
    }

    private CurrencyTotalsByDay toCurrencyTotalsByDay(Money m) {
        return CurrencyTotalsByDay.builder()
                .currency(m.currency().getCurrencyCode())
                .totalValue(m.amount())
                .build();
    }

    private List<AllocationBreakdown> toBreakdowns(List<AllocationBreakdownResult> results) {
        if (results == null) return List.of();
        return results.stream()
                .map(r -> AllocationBreakdown.builder()
                        .assetType(r.assetType().name())
                        .totalValue(r.totalValue().amount())
                        .percentage(r.percentage())
                        .build())
                .toList();
    }
}
