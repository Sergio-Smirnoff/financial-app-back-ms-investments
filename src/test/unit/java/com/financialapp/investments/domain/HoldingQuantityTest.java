package com.financialapp.investments.domain;

import com.financialapp.investments.domain.exception.holding.HoldingQuantityNonPositiveException;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class HoldingQuantityTest {

    @Test
    void nullValue_throws() {
        assertThatThrownBy(() -> new HoldingQuantity(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void zeroValue_throws() {
        assertThatThrownBy(() -> new HoldingQuantity(BigDecimal.ZERO))
                .isInstanceOf(HoldingQuantityNonPositiveException.class);
    }

    @Test
    void negativeValue_throws() {
        assertThatThrownBy(() -> new HoldingQuantity(new BigDecimal("-1")))
                .isInstanceOf(HoldingQuantityNonPositiveException.class);
    }

    @Test
    void positiveValue_accepted() {
        HoldingQuantity q = new HoldingQuantity(new BigDecimal("0.001"));
        assertThat(q.value()).isEqualByComparingTo(new BigDecimal("0.001"));
    }
}
