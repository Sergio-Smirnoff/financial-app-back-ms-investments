package com.financialapp.investments.domain.model.market;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarketIndexTest {

    @Test
    void validMarketIndex_createdSuccessfully() {
        LocalDateTime now = LocalDateTime.now();
        MarketIndex index = new MarketIndex("MERVAL", new BigDecimal("1834520.00"), new BigDecimal("1.20"), now);

        assertThat(index.code()).isEqualTo("MERVAL");
        assertThat(index.value()).isEqualTo(new BigDecimal("1834520.00"));
        assertThat(index.variation()).isEqualTo(new BigDecimal("1.20"));
        assertThat(index.updatedAt()).isEqualTo(now);
    }

    @Test
    void blankCode_throwsException() {
        assertThatThrownBy(() -> new MarketIndex("", new BigDecimal("100"), null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code must not be blank");
    }

    @Test
    void nullValue_throwsException() {
        assertThatThrownBy(() -> new MarketIndex("SP500", null, null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value must not be null");
    }
}
