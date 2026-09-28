package com.financialapp.investments.application.snapshot.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapturePortfolioSnapshotUseCaseImpl implements CapturePortfolioSnapshotUseCase {

    private final HoldingQueryGateway holdingQueryGateway;
    private final GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;
    private final PortfolioSnapshotRepository snapshotRepository;

    @Override
    public SnapshotCaptureResult execute() {
        List<UserId> userIds = holdingQueryGateway.findDistinctUserIds();
        LocalDate today = LocalDate.now();
        int failed = 0;

        for (UserId userId : userIds) {
            try {
                PortfolioSummaryResult summary = getPortfolioSummaryUseCase.execute(
                        new GetPortfolioSummaryCommand(userId));

                List<Money> totals = summary.byCurrency().stream()
                        .map(CurrencyTotals::totalValue)
                        .toList();

                snapshotRepository.save(new PortfolioSnapshot(
                        new PortfolioSnapshotId(null),
                        userId, today,
                        totals,
                        LocalDateTime.now()
                ));
            } catch (RuntimeException e) {
                failed++;
                log.error("Failed to capture portfolio snapshot for user {}", userId.value(), e);
            }
        }
        return new SnapshotCaptureResult(userIds.size(), failed);
    }
}
