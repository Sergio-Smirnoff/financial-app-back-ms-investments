package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.common.model.Money;
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
        BigDecimal totalValueAmount = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;
        Map<AssetType, BigDecimal> valueByType = new EnumMap<>(AssetType.class);

        for (HoldingWithPriceResult item : items) {
            BigDecimal cost = item.holding().avgPurchasePrice().amount()
                    .multiply(item.holding().quantity().value());
            totalValueAmount = totalValueAmount.add(item.currentValue());
            totalCostAmount = totalCostAmount.add(cost);
            valueByType.merge(item.holding().assetType(), item.currentValue(), BigDecimal::add);
        }

        Money totalValue = new Money(totalValueAmount, currency);
        Money totalCost = new Money(totalCostAmount, currency);
        Money totalPl = totalValue.subtract(totalCost);

        BigDecimal plPercent = totalCostAmount.compareTo(BigDecimal.ZERO) != 0
                ? totalPl.amount().divide(totalCostAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<AllocationBreakdownResult> breakdown = buildBreakdown(valueByType, totalValueAmount, currency);

        return new CurrencyTotals(totalValue, totalCost, totalPl, plPercent, breakdown);
    }

    private List<AllocationBreakdownResult> buildBreakdown(
            Map<AssetType, BigDecimal> valueByType, BigDecimal total, Currency currency) {
        return valueByType.entrySet().stream()
                .map(e -> {
                    BigDecimal percentage = total.compareTo(BigDecimal.ZERO) != 0
                            ? e.getValue().divide(total, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;
                    return new AllocationBreakdownResult(
                            e.getKey(), new Money(e.getValue(), currency), percentage);
                })
                .sorted(Comparator.comparing(r -> r.assetType().name()))
                .toList();
    }
}
