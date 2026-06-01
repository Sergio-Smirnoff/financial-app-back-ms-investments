package com.financialapp.investments.domain.usecase.portfolio.response;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.price.AssetType;

import java.math.BigDecimal;
import java.util.Objects;

public record AllocationBreakdownResult(
        AssetType assetType,
        Money totalValue,
        BigDecimal percentage
) {
    public AllocationBreakdownResult {
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(totalValue, "totalValue must not be null");
        Objects.requireNonNull(percentage, "percentage must not be null");
    }
}
