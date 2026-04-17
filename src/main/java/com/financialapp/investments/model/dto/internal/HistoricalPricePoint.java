package com.financialapp.investments.model.dto.internal;

import java.time.LocalDateTime;

public record HistoricalPricePoint(
        LocalDateTime pricedAt,
        PriceDetail detail
) {}
