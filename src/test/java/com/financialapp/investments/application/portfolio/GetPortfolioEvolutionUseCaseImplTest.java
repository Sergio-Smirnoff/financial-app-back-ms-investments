package com.financialapp.investments.application.portfolio;

import com.financialapp.investments.application.portfolio.impl.GetPortfolioEvolutionUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioEvolutionCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPortfolioEvolutionUseCaseImplTest {

    @Mock private PortfolioSnapshotRepository repo;
    @InjectMocks private GetPortfolioEvolutionUseCaseImpl useCase;

    @Test
    void execute_mapsSnapshotsToEvolutionPoints_passesCorrectFromDate() {
        UserId user = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 1, 1);
        PortfolioSnapshot s = new PortfolioSnapshot(new PortfolioSnapshotId(1L), user, date,
                List.of(Money.of(BigDecimal.TEN, "ARS")), LocalDateTime.now());
        when(repo.findByUserIdAndSnapshotDateAfter(eq(user), any())).thenReturn(List.of(s));

        List<PortfolioEvolutionPoint> r = useCase.execute(new GetPortfolioEvolutionCommand(user, 30));

        ArgumentCaptor<LocalDate> dateCap = ArgumentCaptor.forClass(LocalDate.class);
        verify(repo).findByUserIdAndSnapshotDateAfter(eq(user), dateCap.capture());
        assertThat(dateCap.getValue()).isEqualTo(LocalDate.now().minusDays(30));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).date()).isEqualTo(date);
        assertThat(r.get(0).totals()).hasSize(1);
    }
}
