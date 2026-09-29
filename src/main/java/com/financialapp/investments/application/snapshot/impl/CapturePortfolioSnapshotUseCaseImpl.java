package com.financialapp.investments.application.snapshot.impl;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapturePortfolioSnapshotUseCaseImpl implements CapturePortfolioSnapshotUseCase {

    private final HoldingQueryGateway holdingQueryGateway;
    private final EnsurePortfolioSnapshotUseCase ensurePortfolioSnapshotUseCase;

    @Override
    public SnapshotCaptureResult execute() {
        List<UserId> userIds = holdingQueryGateway.findDistinctUserIds();
        int failed = 0;

        for (UserId userId : userIds) {
            try {
                ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(userId));
            } catch (RuntimeException e) {
                failed++;
                log.error("Failed to capture portfolio snapshot for user {}", userId.value(), e);
            }
        }
        return new SnapshotCaptureResult(userIds.size(), failed);
    }
}
