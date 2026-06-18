package com.financialapp.investments.domain.usecase.market.response;

import java.util.List;
import java.util.Objects;

public record MarketDiscoveryResult(
        boolean marketDataAvailable,
        List<MarketOpportunityResult> opportunities) {

    public MarketDiscoveryResult {
        Objects.requireNonNull(opportunities, "opportunities must not be null");
    }
}
