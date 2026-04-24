package com.financialapp.investments.model.dto.internal;

import java.math.BigDecimal;

public record MarketQuote(
    String ticker,
    BigDecimal price,
    BigDecimal variation
) {}
