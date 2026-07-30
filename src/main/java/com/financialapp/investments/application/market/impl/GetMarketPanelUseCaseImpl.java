package com.financialapp.investments.application.market.impl;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.MarketIndexRepository;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.domain.usecase.fx.GetLatestFxRates;
import com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMarketPanelUseCaseImpl implements GetMarketPanelUseCase {

    private final MarketQuoteRepository marketQuoteRepository;
    private final MarketIndexRepository marketIndexRepository;
    private final GetLatestFxRates getLatestFxRates;

    @Override
    public MarketPanelResult execute() {
        List<MarketQuote> quotes = marketQuoteRepository.findAll();
        List<MarketIndex> indices = marketIndexRepository.findAll();
        List<FxRate> fxRates = getLatestFxRates.execute();

        return new MarketPanelResult(quotes, indices, fxRates);
    }
}
