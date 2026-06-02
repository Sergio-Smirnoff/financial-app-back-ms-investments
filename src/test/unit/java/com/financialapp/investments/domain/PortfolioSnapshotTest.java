package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioSnapshotTest {

    private static final UserId USER = new UserId(1L);
    private static final LocalDate DATE = LocalDate.of(2026, 5, 30);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 30, 0, 0);
    private static final Money ARS = Money.of(new BigDecimal("100"), "ARS");

    @Test
    void nullUserId_throws() {
        assertThatThrownBy(() -> new PortfolioSnapshot(null, null, DATE, List.of(ARS), NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullDate_throws() {
        assertThatThrownBy(() -> new PortfolioSnapshot(null, USER, null, List.of(ARS), NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullTotals_throws() {
        assertThatThrownBy(() -> new PortfolioSnapshot(null, USER, DATE, null, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void totals_isDefensiveCopy() {
        PortfolioSnapshot s = new PortfolioSnapshot(new PortfolioSnapshotId(1L), USER, DATE,
                List.of(ARS), NOW);
        assertThatThrownBy(() -> s.totals().add(ARS)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(s.totals()).containsExactly(ARS);
        assertThat(s.snapshotDate()).isEqualTo(DATE);
        assertThat(s.userId()).isEqualTo(USER);
    }
}
