package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
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
                .map(h -> computeResult(h, priceMap.getOrDefault(h.ticker(), h.avgPurchasePrice().amount())))
                .toList();
    }

    private static HoldingWithPriceResult computeResult(Holding holding, BigDecimal currentPrice) {
        BigDecimal quantity = holding.quantity().value();
        BigDecimal avgPrice = holding.avgPurchasePrice().amount();
        BigDecimal currentValue = currentPrice.multiply(quantity);
        BigDecimal costBasis = avgPrice.multiply(quantity);
        BigDecimal plAmount = currentValue.subtract(costBasis);
        BigDecimal plPercent = costBasis.compareTo(BigDecimal.ZERO) != 0
                ? plAmount.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new HoldingWithPriceResult(holding, currentPrice, currentValue, plAmount, plPercent);
    }
}
