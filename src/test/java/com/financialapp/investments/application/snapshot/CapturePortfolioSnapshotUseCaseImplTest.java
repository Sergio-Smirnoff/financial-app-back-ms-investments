package com.financialapp.investments.application.snapshot;

import com.financialapp.investments.application.snapshot.impl.CapturePortfolioSnapshotUseCaseImpl;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapturePortfolioSnapshotUseCaseImplTest {

    @Mock private HoldingQueryGateway holdingQueryGateway;
    @Mock private EnsurePortfolioSnapshotUseCase ensurePortfolioSnapshotUseCase;
    @InjectMocks private CapturePortfolioSnapshotUseCaseImpl useCase;

    @Test
    void execute_ensuresEachHoldersSnapshot() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));

        useCase.execute();

        InOrder order = inOrder(ensurePortfolioSnapshotUseCase);
        order.verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u1));
        order.verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u2));
    }

    @Test
    void execute_perUserFailure_swallowed_continuesOthers() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));
        when(ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(u1)))
                .thenThrow(new RuntimeException("boom"));

        useCase.execute();

        verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u2));
    }
}
