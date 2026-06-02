package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void nullAmount_throws() {
        assertThatThrownBy(() -> new Money(null, ARS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullCurrency_throws() {
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, (Currency) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void of_invalidIsoCode_throws() {
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "XXX_INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_validIsoCode_constructs() {
        Money m = Money.of(BigDecimal.ONE, "ARS");
        assertThat(m.currency()).isEqualTo(ARS);
    }

    @Test
    void zero_factory_constructsZeroMoney() {
        Money m = Money.zero("USD");
        assertThat(m.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.currency()).isEqualTo(USD);
    }

    @Test
    void add_sameCurrency() {
        Money a = new Money(new BigDecimal("100"), ARS);
        Money b = new Money(new BigDecimal("50"), ARS);
        assertThat(a.add(b).amount()).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    void add_differentCurrency_throws() {
        Money a = new Money(BigDecimal.ONE, ARS);
        Money b = new Money(BigDecimal.ONE, USD);
        assertThatThrownBy(() -> a.add(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtract_sameCurrency() {
        Money a = new Money(new BigDecimal("100"), ARS);
        Money b = new Money(new BigDecimal("30"), ARS);
        assertThat(a.subtract(b).amount()).isEqualByComparingTo(new BigDecimal("70"));
    }

    @Test
    void subtract_differentCurrency_throws() {
        Money a = new Money(BigDecimal.ONE, ARS);
        Money b = new Money(BigDecimal.ONE, USD);
        assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multiply_scaledCorrectly() {
        Money m = new Money(new BigDecimal("10"), ARS);
        assertThat(m.multiply(new BigDecimal("3")).amount()).isEqualByComparingTo(new BigDecimal("30"));
    }

    @Test
    void negate_flipsSign() {
        Money m = new Money(new BigDecimal("100"), ARS);
        assertThat(m.negate().amount()).isEqualByComparingTo(new BigDecimal("-100"));
    }
}
