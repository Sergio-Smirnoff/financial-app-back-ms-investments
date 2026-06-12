package com.financialapp.investments.application.market.impl;

import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.domain.usecase.market.SearchTickersUseCase;
import com.financialapp.investments.domain.usecase.market.response.TickerSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchTickersUseCaseImpl implements SearchTickersUseCase {

    private final MarketQuoteRepository marketQuoteRepository;

    @Override
    public List<TickerSearchResult> execute(String query) {
        if (query == null || query.isBlank()) return List.of();
        return marketQuoteRepository.search(query.trim()).stream()
                .map(q -> new TickerSearchResult(q.ticker(), q.price(), q.variation()))
                .toList();
    }
}
