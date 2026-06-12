package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.usecase.holding.GetAccountValuationUseCase;
import com.financialapp.investments.domain.usecase.holding.command.GetAccountValuationCommand;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAccountValuationUseCaseImpl implements GetAccountValuationUseCase {

    private final HoldingQueryGateway holdingQueryGateway;
    private final AssetPriceRepository assetPriceRepository;

    @Override
    public AccountValuationResult execute(GetAccountValuationCommand command) {
        String currencyCode = command.currency().getCurrencyCode();
        List<Holding> holdings = holdingQueryGateway.findByUserIdAndBankNumberAndCurrency(
                command.userId(), command.bankNumber(), command.currency());

        if (holdings.isEmpty()) {
            return new AccountValuationResult(command.bankNumber(), Money.zero(currencyCode), 0);
        }

        Set<Ticker> tickers = holdings.stream().map(Holding::ticker).collect(Collectors.toSet());
        Map<Ticker, BigDecimal> priceMap = assetPriceRepository.findAllByTickerIn(tickers).stream()
                .collect(Collectors.toMap(AssetPrice::ticker, AssetPrice::lastPrice, (a, b) -> b));

        Money totalValuation = holdings.stream()
                .map(holding -> unitPrice(holding, priceMap, currencyCode).multiply(holding.quantity().value()))
                .reduce(Money.zero(currencyCode), Money::add);

        return new AccountValuationResult(command.bankNumber(), totalValuation, holdings.size());
    }

    private Money unitPrice(Holding holding, Map<Ticker, BigDecimal> priceMap, String currencyCode) {
        BigDecimal rawPrice = priceMap.get(holding.ticker());
        if (rawPrice != null) {
            return Money.of(rawPrice, currencyCode);
        }
        return holding.avgPurchasePrice();
    }
}
