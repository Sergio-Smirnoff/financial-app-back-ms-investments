package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.model.market.MarketQuote;

import java.util.List;

public interface GetMarketPanelUseCase {
    record MarketPanelResult(
            List<MarketQuote> quotes,
            List<MarketIndex> indices,
            List<FxRate> fxRates
    ) {}

    MarketPanelResult execute();
}
