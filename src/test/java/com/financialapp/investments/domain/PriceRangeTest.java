package com.financialapp.investments.domain;

import com.financialapp.investments.domain.model.market.PriceRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceRangeTest {

    @Test
    void d30_spans_thirty_days() {
        LocalDate today = LocalDate.of(2026, 6, 11);
        PriceRange thirtyDayRange = PriceRange.of("D30");
        assertThat(thirtyDayRange.from(today)).isEqualTo(LocalDate.of(2026, 5, 12));
        assertThat(thirtyDayRange.to(today)).isEqualTo(today);
    }

    @Test
    void rejects_unknown_code() {
        assertThatThrownBy(() -> PriceRange.of("XX")).isInstanceOf(IllegalArgumentException.class);
    }
}
