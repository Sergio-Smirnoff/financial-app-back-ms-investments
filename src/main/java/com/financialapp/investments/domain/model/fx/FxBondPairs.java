package com.financialapp.investments.domain.model.fx;

public record FxBondPairs(
        String mepArsTicker,
        String mepUsdTicker,
        String cclArsTicker,
        String cclUsdTicker
) {}
