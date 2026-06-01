package com.financialapp.investments.domain.model.holding;

import java.math.BigDecimal;

public record ThresholdConfig(BigDecimal gainPct, BigDecimal lossPct) {

    public ThresholdConfig {
        if (gainPct != null && gainPct.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("gainPct must be >= 0");
        }
        if (lossPct != null && lossPct.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("lossPct must be >= 0");
        }
    }

    public static ThresholdConfig disabled() {
        return new ThresholdConfig(null, null);
    }

    public boolean hasGainThreshold() {
        return gainPct != null;
    }

    public boolean hasLossThreshold() {
        return lossPct != null;
    }
}
