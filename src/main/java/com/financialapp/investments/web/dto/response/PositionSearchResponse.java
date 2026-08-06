package com.financialapp.investments.web.dto.response;

public record PositionSearchResponse(
        Long holdingId,
        String ticker,
        String name,
        String quantity,
        String marketValue,
        String currency
) {}
