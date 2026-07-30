package com.financialapp.investments.application.fx;

import com.financialapp.investments.application.fx.impl.BackfillFxRatesUseCaseImpl;
import com.financialapp.investments.domain.model.fx.*;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.service.FxRateDerivation;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates.BackfillFxRatesCommand;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates.BackfillResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackfillFxRatesUseCaseImplTest {

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private FxRateDerivation derivation;

    private BackfillFxRatesUseCaseImpl useCase;

    private static final LocalDate FROM = LocalDate.of(2026, 7, 20);
    private static final LocalDate TO = LocalDate.of(2026, 7, 21);

    @BeforeEach
    void setUp() {
        useCase = new BackfillFxRatesUseCaseImpl(fxRateRepository, derivation);
    }

    @Test
    void execute_noExistingRates_derivesAndCreatesAllViews() {
        given(fxRateRepository.findByDate(any(), any())).willReturn(Optional.empty());
        given(derivation.deriveRates(any())).willAnswer(inv -> {
            LocalDate d = inv.getArgument(0);
            return new FxSnapshotRates(new BigDecimal("1300"), new BigDecimal("1350"), new BigDecimal("950"), d);
        });

        BackfillResult result = useCase.execute(new BackfillFxRatesCommand(FROM, TO));

        // 2 days * 3 views = 6 rates created
        assertThat(result.createdCount()).isEqualTo(6);
        verify(fxRateRepository, times(6)).save(any());
    }

    @Test
    void execute_existingMep_onlyBackfillsMissingCclAndOficial() {
        LocalDate date = FROM;
        FxRate existingMep = new FxRate(new FxRateId(1L), date, FxView.MEP, new BigDecimal("1300"), new BigDecimal("1300"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());

        given(fxRateRepository.findByDate(date, FxView.MEP)).willReturn(Optional.of(existingMep));
        given(fxRateRepository.findByDate(date, FxView.CCL)).willReturn(Optional.empty());
        given(fxRateRepository.findByDate(date, FxView.OFICIAL)).willReturn(Optional.empty());

        given(derivation.deriveRates(date)).willReturn(new FxSnapshotRates(new BigDecimal("1300"), new BigDecimal("1350"), new BigDecimal("950"), date));

        BackfillResult result = useCase.execute(new BackfillFxRatesCommand(date, date));

        assertThat(result.createdCount()).isEqualTo(2);
        verify(fxRateRepository, times(2)).save(any());
    }

    @Test
    void execute_idempotent_whenAllViewsExistSavesNothing() {
        LocalDate date = FROM;
        FxRate mep = new FxRate(new FxRateId(1L), date, FxView.MEP, new BigDecimal("1300"), new BigDecimal("1300"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());
        FxRate ccl = new FxRate(new FxRateId(2L), date, FxView.CCL, new BigDecimal("1350"), new BigDecimal("1350"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());
        FxRate oficial = new FxRate(new FxRateId(3L), date, FxView.OFICIAL, new BigDecimal("950"), new BigDecimal("950"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());

        given(fxRateRepository.findByDate(date, FxView.MEP)).willReturn(Optional.of(mep));
        given(fxRateRepository.findByDate(date, FxView.CCL)).willReturn(Optional.of(ccl));
        given(fxRateRepository.findByDate(date, FxView.OFICIAL)).willReturn(Optional.of(oficial));

        BackfillResult result = useCase.execute(new BackfillFxRatesCommand(date, date));

        assertThat(result.createdCount()).isEqualTo(0);
        verify(fxRateRepository, never()).save(any());
        verify(derivation, never()).deriveRates(any());
    }
}
