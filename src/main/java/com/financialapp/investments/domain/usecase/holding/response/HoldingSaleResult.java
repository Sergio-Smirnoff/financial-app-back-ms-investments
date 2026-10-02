package com.financialapp.investments.domain.usecase.holding.response;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;

public record HoldingSaleResult(
        Money proceeds,
        Money bookedAmount,
        HoldingQuantity soldQuantity,
        HoldingQuantity remainingQuantity
) {
    public boolean closed() {
        return remainingQuantity == null;
    }
}
