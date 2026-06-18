package com.financialapp.investments.domain.usecase.market.response;

import java.util.List;

public record MarketDiscoveryResult(
        boolean marketDataAvailable,
        List<MarketOpportunityResult> opportunities) {}
