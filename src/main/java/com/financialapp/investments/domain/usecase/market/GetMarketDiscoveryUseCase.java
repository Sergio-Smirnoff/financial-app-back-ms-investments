package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;

import java.util.List;

public interface GetMarketDiscoveryUseCase {

    List<MarketOpportunityResult> execute(GetMarketDiscoveryCommand command);
}
