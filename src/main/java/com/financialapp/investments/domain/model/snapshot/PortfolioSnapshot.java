package com.financialapp.investments.domain.model.snapshot;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record PortfolioSnapshot(
        PortfolioSnapshotId id,
        UserId userId,
        LocalDate snapshotDate,
        List<Money> totals,
        LocalDateTime createdAt
) {
    public PortfolioSnapshot {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(snapshotDate, "snapshotDate must not be null");
        Objects.requireNonNull(totals, "totals must not be null");
        totals = List.copyOf(totals);
    }
}
