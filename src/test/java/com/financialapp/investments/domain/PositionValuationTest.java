package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.model.holding.PositionValuation;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionValuationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 28, 0, 0);

    private static Holding holding(AssetType type, String quantity, String averagePrice) {
        return new Holding(new HoldingId(1L), new UserId(1L), new BankNumber("017"), new Ticker("AO29"),
                "Bono 2029", type, new HoldingQuantity(new BigDecimal(quantity)),
                Money.of(new BigDecimal(averagePrice), "ARS"), ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW);
    }

    @Test
    void bondsAreQuotedPerHundredNominal() {
        Holding ao29 = holding(AssetType.BOND, "687", "1435.78");

        assertThat(ao29.marketValue(Money.of(new BigDecimal("131700"), "ARS")).amount())
                .isEqualByComparingTo("904779.00");
    }

    @Test
    void aBondsUnitPriceIsItsQuoteDividedByHundred() {
        Holding ao29 = holding(AssetType.BOND, "687", "1435.78");

        assertThat(ao29.unitPrice(Money.of(new BigDecimal("131700"), "ARS")).amount())
                .isEqualByComparingTo("1317.00");
        assertThat(holding(AssetType.CEDEAR, "10", "100").unitPrice(Money.of(new BigDecimal("120"), "ARS")).amount())
                .isEqualByComparingTo("120");
    }

    @ParameterizedTest
    @EnumSource(value = AssetType.class, names = {"STOCK", "CEDEAR", "FCI"})
    void everyOtherTypeIsQuotedPerUnit(AssetType type) {
        Holding position = holding(type, "10", "100");

        assertThat(position.marketValue(Money.of(new BigDecimal("120"), "ARS")).amount())
                .isEqualByComparingTo("1200");
    }

    @Test
    void costBasisIsTheAveragePriceTimesTheQuantity() {
        assertThat(holding(AssetType.BOND, "687", "1435.78").costBasis().amount())
                .isEqualByComparingTo("986380.86");
    }

    @Test
    void valuationReportsProfitAndLossAndItsPercentage() {
        PositionValuation valuation = holding(AssetType.BOND, "687", "1435.78")
                .valuation(Money.of(new BigDecimal("131700"), "ARS"));

        assertThat(valuation.marketValue().amount()).isEqualByComparingTo("904779.00");
        assertThat(valuation.costBasis().amount()).isEqualByComparingTo("986380.86");
        assertThat(valuation.profitAndLoss().amount()).isEqualByComparingTo("-81601.86");
        assertThat(valuation.profitAndLossPercent()).isEqualByComparingTo("-8.27");
    }

    @Test
    void aPositionWithoutAQuoteIsValuedAtCost() {
        PositionValuation valuation = holding(AssetType.BOND, "687", "1435.78").valuationAtCost();

        assertThat(valuation.marketValue().amount()).isEqualByComparingTo("986380.86");
        assertThat(valuation.profitAndLoss().amount()).isEqualByComparingTo("0");
        assertThat(valuation.profitAndLossPercent()).isEqualByComparingTo("0");
    }

    @Test
    void aZeroCostHasAZeroPercentage() {
        PositionValuation valuation = new PositionValuation(
                Money.of(new BigDecimal("5"), "ARS"), Money.of(BigDecimal.ZERO, "ARS"));

        assertThat(valuation.profitAndLossPercent()).isEqualByComparingTo("0");
    }

    @Test
    void marketValueAndCostMustShareACurrency() {
        assertThatThrownBy(() -> new PositionValuation(
                Money.of(BigDecimal.ONE, "ARS"), Money.of(BigDecimal.ONE, "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
