package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.web.dto.response.AllocationBreakdown;
import com.financialapp.investments.web.dto.response.CurrencyTotalsByDay;
import com.financialapp.investments.web.dto.response.CurrencyTotalsResponse;
import com.financialapp.investments.web.dto.response.PortfolioEvolutionResponse;
import com.financialapp.investments.web.dto.response.PortfolioSummaryResponse;
import com.financialapp.investments.web.dto.response.PositionSearchResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.financialapp.investments.web.mapper.BigDecimals.toPlain;

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

    public PositionSearchResponse toPositionSearchResponse(Holding holding) {
        BigDecimal marketValue = holding.avgPurchasePrice().amount().multiply(holding.quantity().value());
        return new PositionSearchResponse(
                holding.id() != null ? holding.id().value() : null,
                holding.ticker().value(),
                holding.name(),
                toPlain(holding.quantity().value()),
                toPlain(marketValue),
                holding.avgPurchasePrice().currency().getCurrencyCode()
        );
    }

    private CurrencyTotalsResponse toCurrencyTotalsResponse(CurrencyTotals totals) {
        return CurrencyTotalsResponse.builder()
                .currency(totals.currency().getCurrencyCode())
                .totalValue(toPlain(totals.totalValue().amount()))
                .totalCost(toPlain(totals.totalCost().amount()))
                .totalPl(toPlain(totals.totalPl().amount()))
                .plPercent(toPlain(totals.plPercent()))
                .breakdown(toBreakdowns(totals.breakdown()))
                .build();
    }

    private CurrencyTotalsByDay toCurrencyTotalsByDay(Money m) {
        return CurrencyTotalsByDay.builder()
                .currency(m.currency().getCurrencyCode())
                .totalValue(toPlain(m.amount()))
                .build();
    }

    private List<AllocationBreakdown> toBreakdowns(List<AllocationBreakdownResult> results) {
        if (results == null) return List.of();
        return results.stream()
                .map(r -> AllocationBreakdown.builder()
                        .assetType(r.assetType().name())
                        .totalValue(toPlain(r.totalValue().amount()))
                        .percentage(toPlain(r.percentage()))
                        .build())
                .toList();
    }
}
