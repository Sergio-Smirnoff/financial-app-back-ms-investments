package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.holding.command.GetAccountValuationCommand;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import com.financialapp.investments.domain.usecase.holding.GetAccountValuationUseCase;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
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
            return new AccountValuationResult(command.bankNumber(), BigDecimal.ZERO, currencyCode, 0);
        }

        Set<Ticker> tickers = holdings.stream().map(Holding::ticker).collect(Collectors.toSet());
        Map<Ticker, BigDecimal> priceMap = assetPriceRepository.findAllByTickerIn(tickers).stream()
                .collect(Collectors.toMap(AssetPrice::ticker, AssetPrice::lastPrice, (a, b) -> b));

        BigDecimal totalValuation = holdings.stream()
                .map(h -> priceMap.getOrDefault(h.ticker(), h.avgPurchasePrice().amount())
                        .multiply(h.quantity().value()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountValuationResult(command.bankNumber(), totalValuation, currencyCode, holdings.size());
    }
}
