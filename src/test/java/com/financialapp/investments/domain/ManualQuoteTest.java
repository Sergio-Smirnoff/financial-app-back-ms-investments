package com.financialapp.investments.domain;

import com.financialapp.investments.domain.model.holding.ManualQuote;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualQuoteTest {

    @Test
    void nullValue_throws() {
        assertThatThrownBy(() -> new ManualQuote(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void zeroValue_throws() {
        assertThatThrownBy(() -> new ManualQuote(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeValue_throws() {
        assertThatThrownBy(() -> new ManualQuote(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moreThanSixDecimals_throws() {
        assertThatThrownBy(() -> new ManualQuote(new BigDecimal("10.1234567")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void trailingZerosCountTowardTheSixDecimals() {
        assertThatThrownBy(() -> new ManualQuote(new BigDecimal("10.5000000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sixDecimals_accepted() {
        assertThat(new ManualQuote(new BigDecimal("999999999999.999999")).value())
                .isEqualByComparingTo("999999999999.999999");
    }

    @Test
    void sixDecimalsWithTrailingZeros_accepted() {
        assertThat(new ManualQuote(new BigDecimal("10.500000")).value())
                .isEqualByComparingTo("10.5");
    }
}
