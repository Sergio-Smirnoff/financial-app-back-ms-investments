package com.financialapp.investments.model.dto.response;

import java.math.BigDecimal;

public record AccountValuationResponse(
    Long accountId,
    BigDecimal totalValuation,
    String currency
) {}
