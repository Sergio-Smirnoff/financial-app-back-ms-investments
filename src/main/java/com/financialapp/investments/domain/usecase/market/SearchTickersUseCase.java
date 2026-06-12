package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.usecase.market.response.TickerSearchResult;

import java.util.List;

public interface SearchTickersUseCase {
    List<TickerSearchResult> execute(String query);
}
