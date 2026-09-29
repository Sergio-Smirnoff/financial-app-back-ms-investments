package com.financialapp.investments.application.snapshot;

import com.financialapp.investments.application.snapshot.impl.CapturePortfolioSnapshotUseCaseImpl;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.EnsureSnapshotResult;
import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
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

        SnapshotCaptureResult result = useCase.execute();

        InOrder order = inOrder(ensurePortfolioSnapshotUseCase);
        order.verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u1));
        order.verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u2));
        assertThat(result).isEqualTo(new SnapshotCaptureResult(2, 0));
    }

    @Test
    void execute_perUserFailure_isCounted_andOthersContinue() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));
        when(ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(u1)))
                .thenThrow(new RuntimeException("boom"));

        SnapshotCaptureResult result = useCase.execute();

        verify(ensurePortfolioSnapshotUseCase).execute(new EnsurePortfolioSnapshotCommand(u2));
        assertThat(result).isEqualTo(new SnapshotCaptureResult(2, 1));
    }

    @Test
    void execute_aFailingInsertIsCounted_andTheNextUserIsStillSaved() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));
        when(ensurePortfolioSnapshotUseCase.execute(any()))
                .thenThrow(new IllegalStateException("column \"totals\" is of type jsonb but expression is of type character varying"))
                .thenReturn(new EnsureSnapshotResult(true, LocalDate.of(2026, 9, 22)));

        SnapshotCaptureResult result = useCase.execute();

        verify(ensurePortfolioSnapshotUseCase, times(2)).execute(any());
        assertThat(result).isEqualTo(new SnapshotCaptureResult(2, 1));
    }
}
