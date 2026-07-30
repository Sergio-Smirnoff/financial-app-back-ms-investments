package com.financialapp.investments.domain.service;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.NetPositionResult;
import com.financialapp.investments.domain.model.fee.TradeSide;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BrokerFeeNetting {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal IVA_RATE = new BigDecimal("0.21");

    public NetPositionResult apply(Money grossMagnitude, Money tradeValue, BrokerFeeSchedule schedule, TradeSide side) {
        if (grossMagnitude == null) {
            throw new IllegalArgumentException("grossMagnitude must not be null");
        }
        String currency = grossMagnitude.currency().getCurrencyCode();

        if (schedule == null) {
            return new NetPositionResult(Money.zero(currency), grossMagnitude, false);
        }

        Money baseTradeValue = tradeValue != null ? tradeValue : grossMagnitude;
        BigDecimal tradeValAmount = baseTradeValue.amount();

        BigDecimal sidePct = (side == TradeSide.BUY ? schedule.buyFeePct() : schedule.sellFeePct());
        BigDecimal sideFeeAmount = sidePct != null ? tradeValAmount.multiply(sidePct).divide(HUNDRED, 4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;

        BigDecimal marketPct = schedule.marketFeePct();
        BigDecimal marketFeeAmount = marketPct != null ? tradeValAmount.multiply(marketPct).divide(HUNDRED, 4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;

        BigDecimal rawFeeAmount = sideFeeAmount.add(marketFeeAmount);

        if (schedule.minimumFee() != null) {
            BigDecimal minFeeAmount = schedule.minimumFee().amount();
            if (rawFeeAmount.compareTo(minFeeAmount) < 0) {
                rawFeeAmount = minFeeAmount;
            }
        }

        if (schedule.ivaTreatment() == IvaTreatment.SEPARATE) {
            BigDecimal ivaAmount = rawFeeAmount.multiply(IVA_RATE);
            rawFeeAmount = rawFeeAmount.add(ivaAmount);
        }

        BigDecimal totalFeeAmount = rawFeeAmount.setScale(2, RoundingMode.HALF_EVEN);
        Money totalFee = Money.of(totalFeeAmount, currency);

        BigDecimal grossAmount = grossMagnitude.amount();
        boolean feeExceedsGross = totalFeeAmount.compareTo(grossAmount) > 0;
        BigDecimal netAmount = grossAmount.subtract(totalFeeAmount).abs().setScale(2, RoundingMode.HALF_EVEN);
        Money netMagnitude = Money.of(netAmount, currency);

        return new NetPositionResult(totalFee, netMagnitude, feeExceedsGross);
    }
}
