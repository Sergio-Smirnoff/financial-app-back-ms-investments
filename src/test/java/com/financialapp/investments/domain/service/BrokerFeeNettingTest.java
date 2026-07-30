package com.financialapp.investments.domain.service;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.*;
import com.financialapp.investments.domain.model.price.AssetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BrokerFeeNettingTest {

    private BrokerFeeNetting feeNetting;

    @BeforeEach
    void setUp() {
        feeNetting = new BrokerFeeNetting();
    }

    @Test
    void nullSchedule_returnsZeroFeeAndGrossMagnitude() {
        Money gross = Money.of(new BigDecimal("1000.00"), "ARS");
        NetPositionResult result = feeNetting.apply(gross, gross, null, TradeSide.BUY);

        assertThat(result.totalFee()).isEqualTo(Money.zero("ARS"));
        assertThat(result.netMagnitude()).isEqualTo(gross);
        assertThat(result.feeExceedsGross()).isFalse();
    }

    @Test
    void buySideFee_calculatesPercentageAndMarketFee() {
        Money gross = Money.of(new BigDecimal("10000.00"), "ARS");
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                new BrokerFeeScheduleId(1L),
                new BankNumber("007"),
                AssetType.STOCK,
                new BigDecimal("0.50"), // 0.5% buy = 50.00
                new BigDecimal("0.70"), // sell = 70.00
                null,
                new BigDecimal("0.10"), // 0.1% market = 10.00
                IvaTreatment.EXEMPT
        );

        NetPositionResult result = feeNetting.apply(gross, gross, schedule, TradeSide.BUY);

        // fee = 50 + 10 = 60.00 ARS
        assertThat(result.totalFee()).isEqualTo(Money.of(new BigDecimal("60.00"), "ARS"));
        // net = |10000 - 60| = 9940.00
        assertThat(result.netMagnitude()).isEqualTo(Money.of(new BigDecimal("9940.00"), "ARS"));
        assertThat(result.feeExceedsGross()).isFalse();
    }

    @Test
    void sellSideFee_withMinimumFeeFloorAndIvaSeparate() {
        Money gross = Money.of(new BigDecimal("1000.00"), "ARS");
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                new BrokerFeeScheduleId(1L),
                new BankNumber("007"),
                AssetType.STOCK,
                new BigDecimal("0.50"),
                new BigDecimal("0.50"), // 0.5% of 1000 = 5.00
                Money.of(new BigDecimal("100.00"), "ARS"), // minimum fee floor = 100.00
                BigDecimal.ZERO,
                IvaTreatment.SEPARATE // adds 21% to fee => 100 * 1.21 = 121.00
        );

        NetPositionResult result = feeNetting.apply(gross, gross, schedule, TradeSide.SELL);

        assertThat(result.totalFee()).isEqualTo(Money.of(new BigDecimal("121.00"), "ARS"));
        assertThat(result.netMagnitude()).isEqualTo(Money.of(new BigDecimal("879.00"), "ARS"));
        assertThat(result.feeExceedsGross()).isFalse();
    }

    @Test
    void feeExceedsGross_setsFlagAndReturnsPositiveNetMagnitude() {
        Money gross = Money.of(new BigDecimal("50.00"), "ARS");
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                new BrokerFeeScheduleId(1L),
                new BankNumber("007"),
                AssetType.BOND,
                new BigDecimal("1.00"),
                new BigDecimal("1.00"),
                Money.of(new BigDecimal("100.00"), "ARS"), // minimum fee = 100.00
                BigDecimal.ZERO,
                IvaTreatment.EXEMPT
        );

        NetPositionResult result = feeNetting.apply(gross, gross, schedule, TradeSide.BUY);

        // total fee = 100.00 ARS
        assertThat(result.totalFee()).isEqualTo(Money.of(new BigDecimal("100.00"), "ARS"));
        // net magnitude = |50 - 100| = 50.00 ARS
        assertThat(result.netMagnitude()).isEqualTo(Money.of(new BigDecimal("50.00"), "ARS"));
        assertThat(result.feeExceedsGross()).isTrue();
    }
}
