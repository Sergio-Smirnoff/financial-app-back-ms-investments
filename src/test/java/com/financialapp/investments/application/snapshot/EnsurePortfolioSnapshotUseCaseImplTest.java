package com.financialapp.investments.application.snapshot;

import com.financialapp.investments.application.snapshot.impl.EnsurePortfolioSnapshotUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.EnsureSnapshotResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnsurePortfolioSnapshotUseCaseImplTest {

    private static final Clock LATE_EVENING_ART =
            Clock.fixed(Instant.parse("2026-09-23T02:30:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
    private static final LocalDate ART_DAY = LocalDate.of(2026, 9, 22);
    private static final UserId USER = new UserId(7L);
    private static final EnsurePortfolioSnapshotCommand COMMAND = new EnsurePortfolioSnapshotCommand(USER);

    @Mock private GetPortfolioSummaryUseCase summaryUseCase;
    @Mock private PortfolioSnapshotRepository snapshotRepository;
    private EnsurePortfolioSnapshotUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnsurePortfolioSnapshotUseCaseImpl(summaryUseCase, snapshotRepository, LATE_EVENING_ART);
    }

    private static PortfolioSummaryResult summaryOf(String... currencies) {
        return new PortfolioSummaryResult(Arrays.stream(currencies).map(code -> {
            Money money = Money.of(new BigDecimal("100"), code);
            return new CurrencyTotals(money, money, money, BigDecimal.ZERO, List.of());
        }).toList());
    }

    @Test
    void execute_existingSnapshotForTheArtDay_isANoOp() {
        when(snapshotRepository.existsForDate(USER, ART_DAY)).thenReturn(true);

        assertThat(useCase.execute(COMMAND)).isEqualTo(new EnsureSnapshotResult(false, ART_DAY));
        verifyNoInteractions(summaryUseCase);
        verify(snapshotRepository, never()).saveIfAbsent(any());
    }

    @Test
    void execute_missingSnapshot_capturesTheCallersTotalsUnderTheArtDateStampedInUtc() {
        when(snapshotRepository.existsForDate(USER, ART_DAY)).thenReturn(false);
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(USER))).thenReturn(summaryOf("ARS", "USD"));
        when(snapshotRepository.saveIfAbsent(any())).thenReturn(true);

        assertThat(useCase.execute(COMMAND)).isEqualTo(new EnsureSnapshotResult(true, ART_DAY));
        ArgumentCaptor<PortfolioSnapshot> saved = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).saveIfAbsent(saved.capture());
        assertThat(saved.getValue().userId()).isEqualTo(USER);
        assertThat(saved.getValue().snapshotDate()).isEqualTo(ART_DAY);
        assertThat(saved.getValue().totals()).hasSize(2);
        assertThat(saved.getValue().createdAt()).isEqualTo(LocalDateTime.of(2026, 9, 23, 2, 30));
    }

    @Test
    void execute_lostRaceToAnotherCaller_reportsNotCreated() {
        when(snapshotRepository.existsForDate(USER, ART_DAY)).thenReturn(false);
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(USER))).thenReturn(summaryOf("ARS"));
        when(snapshotRepository.saveIfAbsent(any())).thenReturn(false);

        assertThat(useCase.execute(COMMAND)).isEqualTo(new EnsureSnapshotResult(false, ART_DAY));
    }

    @Test
    void execute_failingInsert_propagatesSoTheCaptureRunCanCountIt() {
        when(snapshotRepository.existsForDate(USER, ART_DAY)).thenReturn(false);
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(USER))).thenReturn(summaryOf("ARS"));
        when(snapshotRepository.saveIfAbsent(any()))
                .thenThrow(new IllegalStateException("column \"totals\" is of type jsonb but expression is of type character varying"));

        assertThatThrownBy(() -> useCase.execute(COMMAND)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void execute_userWithoutHoldings_createsNothing() {
        when(snapshotRepository.existsForDate(USER, ART_DAY)).thenReturn(false);
        when(summaryUseCase.execute(new GetPortfolioSummaryCommand(USER))).thenReturn(summaryOf());

        assertThat(useCase.execute(COMMAND)).isEqualTo(new EnsureSnapshotResult(false, ART_DAY));
        verify(snapshotRepository, never()).saveIfAbsent(any());
    }
}
