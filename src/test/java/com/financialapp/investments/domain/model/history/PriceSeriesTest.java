package com.financialapp.investments.domain.model.history;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceSeriesTest {
    private HistoricalPricePoint point(String dateTime, String price) {
        return new HistoricalPricePoint(new BigDecimal(price), null, null, null, null, null,
                "ARS", LocalDateTime.parse(dateTime));
    }

    @Test
    void normalizesToOneClosingPointPerDaySortedAscending() {
        PriceSeries series = new PriceSeries(List.of(
            point("2026-04-15T17:00:00", "110"),
            point("2026-04-14T11:00:00", "100"),
            point("2026-04-14T17:00:00", "105"),
            point("2026-04-15T11:00:00", "108")));

        assertThat(series.points()).hasSize(2);
        assertThat(series.points().get(0).pricedAt().toLocalDate().toString()).isEqualTo("2026-04-14");
        assertThat(series.points().get(0).lastPrice()).isEqualByComparingTo("105");
        assertThat(series.points().get(1).pricedAt().toLocalDate().toString()).isEqualTo("2026-04-15");
        assertThat(series.points().get(1).lastPrice()).isEqualByComparingTo("110");
    }

    @Test
    void rejectsNullPoints() {
        assertThatThrownBy(() -> new PriceSeries(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void emptyInputProducesEmptySeries() {
        PriceSeries series = new PriceSeries(List.of());
        assertThat(series.isEmpty()).isTrue();
        assertThat(series.points()).isEmpty();
    }
}
