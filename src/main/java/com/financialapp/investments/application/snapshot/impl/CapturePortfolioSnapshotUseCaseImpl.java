package com.financialapp.investments.application.snapshot.impl;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapturePortfolioSnapshotUseCaseImpl implements CapturePortfolioSnapshotUseCase {

    private final HoldingQueryGateway holdingQueryGateway;
    private final EnsurePortfolioSnapshotUseCase ensurePortfolioSnapshotUseCase;

    @Override
    public void execute() {
        for (UserId userId : holdingQueryGateway.findDistinctUserIds()) {
            try {
                ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(userId));
            } catch (RuntimeException e) {
                log.error("Failed to capture portfolio snapshot for user {}", userId.value(), e);
            }
        }
    }
}
