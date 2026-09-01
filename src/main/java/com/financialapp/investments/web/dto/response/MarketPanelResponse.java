package com.financialapp.investments.web.dto.response;

import java.util.List;

public record MarketPanelResponse(
        List<MarketQuotePanelResponse> quotes,
        List<MarketIndexResponse> indices,
        List<FxRateResponse> fxRates
) {}
