package com.financialapp.investments.infrastructure.gateway.dto;

import java.time.LocalDateTime;

public record IolHistoricalPricePoint(
        LocalDateTime pricedAt,
        IolPriceDetail detail
) {}
