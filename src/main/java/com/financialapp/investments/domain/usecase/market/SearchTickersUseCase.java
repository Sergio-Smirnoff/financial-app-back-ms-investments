package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.model.market.MarketQuote;

import java.util.List;

public interface SearchTickersUseCase {
    List<MarketQuote> execute(String query);
}
