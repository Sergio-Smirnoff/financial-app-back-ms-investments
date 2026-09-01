package com.financialapp.investments.domain.model.fx;

import com.financialapp.investments.domain.exception.InvalidFxRateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FxRateTest {

    private static final LocalDate DATE = LocalDate.of(2026, 7, 29);
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void validFxRate_createsSuccessfully() {
        FxRate rate = new FxRate(
                new FxRateId(1L),
                DATE,
                FxView.MEP,
                new BigDecimal("1350.0000"),
                new BigDecimal("1360.0000"),
                FxRateSource.IOL_SYNTHETIC,
                NOW
        );

        assertThat(rate.id().value()).isEqualTo(1L);
        assertThat(rate.date()).isEqualTo(DATE);
        assertThat(rate.view()).isEqualTo(FxView.MEP);
        assertThat(rate.buy()).isEqualTo(new BigDecimal("1350.0000"));
        assertThat(rate.sell()).isEqualTo(new BigDecimal("1360.0000"));
        assertThat(rate.source()).isEqualTo(FxRateSource.IOL_SYNTHETIC);
        assertThat(rate.createdAt()).isEqualTo(NOW);
    }

    @Test
    void validFxRate_equalBuyAndSell_allowed() {
        FxRate rate = new FxRate(
                null,
                DATE,
                FxView.CCL,
                new BigDecimal("1400.0000"),
                new BigDecimal("1400.0000"),
                FxRateSource.IOL_SYNTHETIC,
                null
        );

        assertThat(rate.buy()).isEqualTo(rate.sell());
        assertThat(rate.createdAt()).isNotNull();
    }

    @Test
    void nullFields_throwInvalidFxRateException() {
        assertThatThrownBy(() -> new FxRate(null, null, FxView.MEP, new BigDecimal("10"), new BigDecimal("10"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("date");

        assertThatThrownBy(() -> new FxRate(null, DATE, null, new BigDecimal("10"), new BigDecimal("10"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("view");

        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, null, new BigDecimal("10"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("buy");

        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, new BigDecimal("10"), null, FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("sell");

        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, new BigDecimal("10"), new BigDecimal("10"), null, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("source");
    }

    @Test
    void nonPositiveRates_throwInvalidFxRateException() {
        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, BigDecimal.ZERO, new BigDecimal("10"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("buy must be positive");

        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, new BigDecimal("-1"), new BigDecimal("10"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("buy must be positive");

        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, new BigDecimal("10"), BigDecimal.ZERO, FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("sell must be positive");
    }

    @Test
    void sellLessThanBuy_throwsInvalidFxRateException() {
        assertThatThrownBy(() -> new FxRate(null, DATE, FxView.MEP, new BigDecimal("1400.00"), new BigDecimal("1350.00"), FxRateSource.IOL_SYNTHETIC, NOW))
                .isInstanceOf(InvalidFxRateException.class)
                .hasMessageContaining("sell must be greater than or equal to buy");
    }
}
