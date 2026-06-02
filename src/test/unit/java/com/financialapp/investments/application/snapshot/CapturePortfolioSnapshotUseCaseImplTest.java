package com.financialapp.investments.application.snapshot;

import com.financialapp.investments.application.snapshot.impl.CapturePortfolioSnapshotUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapturePortfolioSnapshotUseCaseImplTest {

    @Mock private HoldingQueryGateway holdingQueryGateway;
    @Mock private GetPortfolioSummaryUseCase summaryUseCase;
    @Mock private PortfolioSnapshotRepository snapshotRepository;
    @InjectMocks private CapturePortfolioSnapshotUseCaseImpl useCase;

    @Test
    void execute_savesSnapshotPerUser() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));
        Money m = Money.of(new BigDecimal("100"), "ARS");
        CurrencyTotals ct = new CurrencyTotals(m, m, m, BigDecimal.ZERO, List.of());
        when(summaryUseCase.execute(any())).thenReturn(new PortfolioSummaryResult(List.of(ct)));

        useCase.execute();

        ArgumentCaptor<PortfolioSnapshot> cap = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository, times(2)).save(cap.capture());
        assertThat(cap.getAllValues()).extracting(PortfolioSnapshot::userId).containsExactly(u1, u2);
        assertThat(cap.getAllValues()).allSatisfy(s -> assertThat(s.totals()).hasSize(1));
    }

    @Test
    void execute_perUserFailure_swallowed_continuesOthers() {
        UserId u1 = new UserId(1L);
        UserId u2 = new UserId(2L);
        when(holdingQueryGateway.findDistinctUserIds()).thenReturn(List.of(u1, u2));
        Money m = Money.of(new BigDecimal("100"), "ARS");
        CurrencyTotals ct = new CurrencyTotals(m, m, m, BigDecimal.ZERO, List.of());
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(u1)))
                .thenThrow(new RuntimeException("boom"));
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(u2)))
                .thenReturn(new PortfolioSummaryResult(List.of(ct)));

        useCase.execute();

        verify(snapshotRepository, times(1)).save(any());
    }
}
