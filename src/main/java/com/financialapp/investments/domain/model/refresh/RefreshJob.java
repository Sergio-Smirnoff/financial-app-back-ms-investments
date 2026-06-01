package com.financialapp.investments.domain.model.refresh;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record RefreshJob(
        RefreshJobId id,
        RefreshJobStatus status,
        List<String> allTickers,
        int lastSuccessIndex,
        String failureReason,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {
    public RefreshJob {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(allTickers, "allTickers must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        allTickers = List.copyOf(allTickers);
    }

    public static RefreshJob start(List<String> tickers) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshJob(null, RefreshJobStatus.IN_PROGRESS, tickers, -1, null, now, null, now);
    }

    public RefreshJob advance(int newLastSuccessIndex) {
        return new RefreshJob(id, status, allTickers, newLastSuccessIndex,
                failureReason, startedAt, completedAt, LocalDateTime.now());
    }

    public RefreshJob complete() {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshJob(id, RefreshJobStatus.COMPLETED, allTickers, lastSuccessIndex,
                null, startedAt, now, now);
    }

    public RefreshJob interrupt(String reason) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshJob(id, RefreshJobStatus.INTERRUPTED, allTickers, lastSuccessIndex,
                reason, startedAt, now, now);
    }

    public boolean isComplete() {
        return lastSuccessIndex >= allTickers.size() - 1;
    }
}
