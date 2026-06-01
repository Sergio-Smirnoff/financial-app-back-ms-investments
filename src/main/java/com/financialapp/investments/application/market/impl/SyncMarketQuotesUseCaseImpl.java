package com.financialapp.investments.application.market.impl;

import com.financialapp.investments.domain.usecase.market.SyncMarketQuotesUseCase;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncMarketQuotesUseCaseImpl implements SyncMarketQuotesUseCase {

    private final IolGateway iolGateway;
    private final MarketQuoteRepository marketQuoteRepository;

    @Override
    public void execute() {
        List<MarketQuote> quotes;
        try {
            quotes = iolGateway.getPanelQuotes("merval");
        } catch (InfrastructureException e) {
            throw new IolServiceException("Failed to sync market quotes from IOL", e);
        }
        marketQuoteRepository.saveAll(quotes);
    }
}
