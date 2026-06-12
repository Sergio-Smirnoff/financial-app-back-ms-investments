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
    void y1_spans_one_calendar_year() {
        LocalDate today = LocalDate.of(2026, 6, 11);
        assertThat(PriceRange.of("Y1").from(today)).isEqualTo(LocalDate.of(2025, 6, 11));
    }

    @Test
    void all_spans_five_calendar_years() {
        LocalDate today = LocalDate.of(2026, 6, 11);
        assertThat(PriceRange.of("ALL").from(today)).isEqualTo(LocalDate.of(2021, 6, 11));
    }

    @Test
    void rejects_unknown_code() {
        assertThatThrownBy(() -> PriceRange.of("XX")).isInstanceOf(IllegalArgumentException.class);
    }
}
