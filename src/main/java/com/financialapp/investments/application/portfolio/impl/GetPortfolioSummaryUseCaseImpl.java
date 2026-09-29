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
        BigDecimal totalValueAmount = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;
        Map<AssetType, BigDecimal> valueByType = new EnumMap<>(AssetType.class);

        for (HoldingWithPriceResult item : items) {
            totalValueAmount = totalValueAmount.add(item.currentValue());
            totalCostAmount = totalCostAmount.add(item.holding().costBasis().amount());
            valueByType.merge(item.holding().assetType(), item.currentValue(), BigDecimal::add);
        }

        PositionValuation totals = new PositionValuation(
                new Money(totalValueAmount, currency), new Money(totalCostAmount, currency));
        List<AllocationBreakdownResult> breakdown = buildBreakdown(valueByType, totalValueAmount, currency);

        return new CurrencyTotals(totals.marketValue(), totals.costBasis(), totals.profitAndLoss(),
                totals.profitAndLossPercent(), breakdown);
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
