package com.financialapp.investments.domain;

import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.model.refresh.RefreshJobId;
import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshJobTest {

    @Test
    void start_factory_initialState() {
        RefreshJob j = RefreshJob.start(List.of("AAPL", "GOOG"));
        assertThat(j.status()).isEqualTo(RefreshJobStatus.IN_PROGRESS);
        assertThat(j.allTickers()).containsExactly("AAPL", "GOOG");
        assertThat(j.lastSuccessIndex()).isEqualTo(-1);
        assertThat(j.failureReason()).isNull();
        assertThat(j.completedAt()).isNull();
        assertThat(j.startedAt()).isNotNull();
        assertThat(j.updatedAt()).isNotNull();
    }

    @Test
    void advance_updatesIndex_keepsStatus() {
        RefreshJob j = RefreshJob.start(List.of("AAPL", "GOOG")).advance(0);
        assertThat(j.lastSuccessIndex()).isZero();
        assertThat(j.status()).isEqualTo(RefreshJobStatus.IN_PROGRESS);
    }

    @Test
    void complete_setsCompletedStatusAndTimestamp() {
        RefreshJob j = RefreshJob.start(List.of("X")).complete();
        assertThat(j.status()).isEqualTo(RefreshJobStatus.COMPLETED);
        assertThat(j.completedAt()).isNotNull();
        assertThat(j.failureReason()).isNull();
    }

    @Test
    void interrupt_storesReasonAndStatus() {
        RefreshJob j = RefreshJob.start(List.of("X")).interrupt("boom");
        assertThat(j.status()).isEqualTo(RefreshJobStatus.INTERRUPTED);
        assertThat(j.failureReason()).isEqualTo("boom");
        assertThat(j.completedAt()).isNotNull();
    }

    @Test
    void isComplete_trueWhenIndexAtLast() {
        RefreshJob j = RefreshJob.start(List.of("A", "B")).advance(1);
        assertThat(j.isComplete()).isTrue();
    }

    @Test
    void isComplete_falseWhenIndexBelow() {
        RefreshJob j = RefreshJob.start(List.of("A", "B")).advance(0);
        assertThat(j.isComplete()).isFalse();
    }

    @Test
    void compactConstructor_nullStatus_throws() {
        assertThatThrownBy(() -> new RefreshJob(null, null, List.of("A"), -1, null,
                LocalDateTime.now(), null, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void compactConstructor_nullTickers_throws() {
        assertThatThrownBy(() -> new RefreshJob(null, RefreshJobStatus.PENDING, null, -1, null,
                LocalDateTime.now(), null, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void compactConstructor_nullStartedAt_throws() {
        assertThatThrownBy(() -> new RefreshJob(null, RefreshJobStatus.PENDING, List.of("A"), -1, null,
                null, null, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void allTickers_isDefensiveCopy() {
        RefreshJob j = new RefreshJob(new RefreshJobId(1L), RefreshJobStatus.PENDING,
                List.of("A"), -1, null, LocalDateTime.now(), null, LocalDateTime.now());
        assertThatThrownBy(() -> j.allTickers().add("B")).isInstanceOf(UnsupportedOperationException.class);
    }
}
