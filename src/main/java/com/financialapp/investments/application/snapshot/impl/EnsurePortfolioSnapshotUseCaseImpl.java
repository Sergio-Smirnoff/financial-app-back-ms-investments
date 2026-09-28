package com.financialapp.investments.application.snapshot.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.EnsureSnapshotResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnsurePortfolioSnapshotUseCaseImpl implements EnsurePortfolioSnapshotUseCase {

    private final GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final Clock clock;

    @Override
    public EnsureSnapshotResult execute(EnsurePortfolioSnapshotCommand command) {
        LocalDate today = LocalDate.now(clock);
        if (snapshotRepository.existsForDate(command.userId(), today)) {
            return new EnsureSnapshotResult(false, today);
        }
        List<Money> totals = getPortfolioSummaryUseCase.execute(new GetPortfolioSummaryCommand(command.userId()))
                .byCurrency().stream()
                .map(CurrencyTotals::totalValue)
                .toList();
        if (totals.isEmpty()) {
            return new EnsureSnapshotResult(false, today);
        }
        boolean created = snapshotRepository.saveIfAbsent(new PortfolioSnapshot(
                new PortfolioSnapshotId(null), command.userId(), today, totals, LocalDateTime.now(clock.withZone(ZoneOffset.UTC))));
        return new EnsureSnapshotResult(created, today);
    }
}
