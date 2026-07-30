package com.financialapp.investments.domain.model.fee;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.price.AssetType;

import java.math.BigDecimal;

public record BrokerFeeSchedule(
        BrokerFeeScheduleId id,
        BankNumber bankNumber,
        AssetType assetType,
        BigDecimal buyFeePct,
        BigDecimal sellFeePct,
        Money minimumFee,
        BigDecimal marketFeePct,
        IvaTreatment ivaTreatment
) {
    public BrokerFeeSchedule {
        if (bankNumber == null) {
            throw new IllegalArgumentException("bankNumber must not be null");
        }
        validatePct("buyFeePct", buyFeePct);
        validatePct("sellFeePct", sellFeePct);
        validatePct("marketFeePct", marketFeePct);
        if (ivaTreatment == null) {
            ivaTreatment = IvaTreatment.SEPARATE;
        }
    }

    private static void validatePct(String name, BigDecimal pct) {
        if (pct != null && (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException(name + " must be between 0 and 100");
        }
    }
}
