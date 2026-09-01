package com.financialapp.investments.web.dto.response;

public record MarketQuotePanelResponse(
        String ticker,
        String price,
        String variation
) {}
