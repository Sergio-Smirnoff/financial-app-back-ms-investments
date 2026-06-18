package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.response.MarketDiscoveryResult;

public interface GetMarketDiscoveryUseCase {

    MarketDiscoveryResult execute(GetMarketDiscoveryCommand command);
}
