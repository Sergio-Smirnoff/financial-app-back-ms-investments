package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.PositionValuation;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPortfolioSummaryUseCaseImpl implements GetPortfolioSummaryUseCase {

    private final GetHoldingsWithPricesUseCase getHoldingsWithPricesUseCase;

    @Override
    public PortfolioSummaryResult execute(GetPortfolioSummaryCommand command) {
        List<HoldingWithPriceResult> holdings = getHoldingsWithPricesUseCase.execute(
                new GetHoldingsWithPricesCommand(command.userId()));

        Map<Currency, List<HoldingWithPriceResult>> byCurrency = holdings.stream()
                .collect(Collectors.groupingBy(h -> h.holding().avgPurchasePrice().currency()));

        List<CurrencyTotals> totals = byCurrency.entrySet().stream()
                .map(e -> computeTotals(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(t -> t.currency().getCurrencyCode()))
                .toList();

        return new PortfolioSummaryResult(totals);
    }

    private CurrencyTotals computeTotals(Currency currency, List<HoldingWithPriceResult> items) {
        PositionValuation totals = valuationOf(items, currency);
        List<AllocationBreakdownResult> breakdown = buildBreakdown(items, totals.marketValue());

        return new CurrencyTotals(totals.marketValue(), totals.costBasis(), totals.profitAndLoss(),
                totals.profitAndLossPercent(), breakdown);
    }

    private List<AllocationBreakdownResult> buildBreakdown(List<HoldingWithPriceResult> items, Money total) {
        Map<AssetType, List<HoldingWithPriceResult>> byType = items.stream()
                .collect(Collectors.groupingBy(item -> item.holding().assetType(),
                        () -> new EnumMap<>(AssetType.class), Collectors.toList()));
        return byType.entrySet().stream()
                .map(e -> {
                    PositionValuation valuation = valuationOf(e.getValue(), total.currency());
                    return new AllocationBreakdownResult(e.getKey(), valuation.marketValue(),
                            valuation.costBasis(), valuation.profitAndLoss(),
                            shareOf(valuation.marketValue(), total), e.getValue().size());
                })
                .sorted(Comparator.comparing(r -> r.assetType().name()))
                .toList();
    }

    private static PositionValuation valuationOf(List<HoldingWithPriceResult> items, Currency currency) {
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        for (HoldingWithPriceResult item : items) {
            value = value.add(item.currentValue());
            cost = cost.add(item.holding().costBasis().amount());
        }
        return new PositionValuation(new Money(value, currency), new Money(cost, currency));
    }

    private static BigDecimal shareOf(Money part, Money total) {
        if (total.amount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.amount().divide(total.amount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
