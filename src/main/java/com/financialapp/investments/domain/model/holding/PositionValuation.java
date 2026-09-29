package com.financialapp.investments.domain.model.holding;

import com.financialapp.investments.domain.common.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record PositionValuation(Money marketValue, Money costBasis) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public PositionValuation {
        Objects.requireNonNull(marketValue, "marketValue must not be null");
        Objects.requireNonNull(costBasis, "costBasis must not be null");
        if (!marketValue.currency().equals(costBasis.currency())) {
            throw new IllegalArgumentException("marketValue and costBasis must share a currency: "
                    + marketValue.currency() + " vs " + costBasis.currency());
        }
    }

    public Money profitAndLoss() {
        return marketValue.subtract(costBasis);
    }

    public BigDecimal profitAndLossPercent() {
        if (costBasis.amount().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return profitAndLoss().amount()
                .divide(costBasis.amount(), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED);
    }
}
