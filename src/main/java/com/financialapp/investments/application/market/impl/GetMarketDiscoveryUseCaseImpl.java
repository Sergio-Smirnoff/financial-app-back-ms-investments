package com.financialapp.investments.application.market.impl;

import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.response.MarketDiscoveryResult;
import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;
import com.financialapp.investments.domain.usecase.market.GetMarketDiscoveryUseCase;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMarketDiscoveryUseCaseImpl implements GetMarketDiscoveryUseCase {

    private final MarketQuoteRepository marketQuoteRepository;
    private final HoldingRepository holdingRepository;

    @Override
    public MarketDiscoveryResult execute(GetMarketDiscoveryCommand command) {
        Set<Ticker> ownedTickers = holdingRepository.findByUserId(command.userId())
                .stream()
                .map(Holding::ticker)
                .collect(Collectors.toSet());

        List<MarketQuote> allQuotes = marketQuoteRepository.findAll();

        List<MarketOpportunityResult> opportunities = allQuotes.stream()
                .filter(q -> !ownedTickers.contains(q.ticker()))
                .sorted(Comparator.comparing(
                        (MarketQuote q) -> q.variation() != null
                                ? q.variation().abs()
                                : BigDecimal.ZERO,
                        Comparator.reverseOrder()))
                .limit(command.limit())
                .map(q -> new MarketOpportunityResult(q.ticker(), q.price(), q.variation(), q.volume()))
                .toList();

        return new MarketDiscoveryResult(!allQuotes.isEmpty(), opportunities);
    }
}
