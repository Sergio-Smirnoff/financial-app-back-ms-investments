package com.financialapp.investments.domain.model.fee;

import com.financialapp.investments.domain.common.model.Money;

public record NetPositionResult(
        Money totalFee,
        Money netMagnitude,
        boolean feeExceedsGross
) {}
