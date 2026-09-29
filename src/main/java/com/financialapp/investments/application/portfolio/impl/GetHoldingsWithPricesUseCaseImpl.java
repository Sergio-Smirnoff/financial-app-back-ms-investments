package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.PositionValuation;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetHoldingsWithPricesUseCaseImpl implements GetHoldingsWithPricesUseCase {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Override
    public List<HoldingWithPriceResult> execute(GetHoldingsWithPricesCommand command) {
        List<Holding> holdings = holdingRepository.findByUserId(command.userId());

        Set<Ticker> tickers = holdings.stream()
                .map(Holding::ticker)
                .collect(Collectors.toSet());

        Map<Ticker, BigDecimal> priceMap = assetPriceRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(AssetPrice::ticker, AssetPrice::lastPrice, (a, b) -> b));

        return holdings.stream()
                .map(holding -> resultFor(holding, Optional.ofNullable(priceMap.get(holding.ticker()))))
                .toList();
    }

    private static HoldingWithPriceResult resultFor(Holding holding, Optional<BigDecimal> quote) {
        PositionValuation valuation = quote
                .map(price -> holding.valuation(new Money(price, holding.avgPurchasePrice().currency())))
                .orElseGet(holding::valuationAtCost);
        return new HoldingWithPriceResult(
                holding,
                quote.orElse(holding.avgPurchasePrice().amount()),
                valuation.marketValue().amount(),
                valuation.profitAndLoss().amount(),
                valuation.profitAndLossPercent());
    }
}
