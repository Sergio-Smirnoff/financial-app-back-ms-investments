package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.exception.ResourceConflictException;
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
        List<Holding> holdings = holdingQueryGateway.findByAccountCbu(command.accountCbu());

        if (holdings.isEmpty()) {
            return new AccountValuationResult(command.accountCbu(), BigDecimal.ZERO, "ARS", 0);
        }

        long distinctCurrencies = holdings.stream()
                .map(h -> h.avgPurchasePrice().currency().getCurrencyCode())
                .distinct()
                .count();
        if (distinctCurrencies > 1) {
            throw new ResourceConflictException(
                    "Account " + command.accountCbu().value()
                            + " holds assets in multiple currencies; use portfolio summary instead.");
        }

        Set<Ticker> tickers = holdings.stream()
                .map(Holding::ticker)
                .collect(Collectors.toSet());

        Map<Ticker, BigDecimal> priceMap = assetPriceRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(AssetPrice::ticker, AssetPrice::lastPrice, (a, b) -> b));

        BigDecimal totalValuation = holdings.stream()
                .map(h -> {
                    BigDecimal price = priceMap.getOrDefault(
                            h.ticker(), h.avgPurchasePrice().amount());
                    return price.multiply(h.quantity().value());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = holdings.getFirst().avgPurchasePrice().currency().getCurrencyCode();

        return new AccountValuationResult(command.accountCbu(), totalValuation, currency, holdings.size());
    }
}
