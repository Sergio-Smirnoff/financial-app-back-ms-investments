package com.financialapp.investments.application.fx;

import com.financialapp.investments.application.fx.impl.GetFxRatesAtDateUseCaseImpl;
import com.financialapp.investments.application.fx.impl.GetFxRatesUseCaseImpl;
import com.financialapp.investments.application.fx.impl.GetLatestFxRatesUseCaseImpl;
import com.financialapp.investments.domain.model.fx.*;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.service.FxRateDerivation;
import com.financialapp.investments.domain.usecase.fx.GetFxRates.GetFxRatesCommand;
import com.financialapp.investments.domain.usecase.fx.GetFxRatesAtDate.GetFxRatesAtDateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FxRatesUseCasesTest {

    private FxRateRepository fxRateRepository;
    private FxRateDerivation fxRateDerivation;

    private GetFxRatesUseCaseImpl getFxRatesUseCase;
    private GetLatestFxRatesUseCaseImpl getLatestFxRatesUseCase;
    private GetFxRatesAtDateUseCaseImpl getFxRatesAtDateUseCase;

    @BeforeEach
    void setUp() {
        fxRateRepository = mock(FxRateRepository.class);
        fxRateDerivation = mock(FxRateDerivation.class);

        getFxRatesUseCase = new GetFxRatesUseCaseImpl(fxRateRepository);
        getLatestFxRatesUseCase = new GetLatestFxRatesUseCaseImpl(fxRateRepository);
        getFxRatesAtDateUseCase = new GetFxRatesAtDateUseCaseImpl(fxRateDerivation);
    }

    @Test
    void getFxRates_queriesRepositoryByDateRangeAndView() {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        FxRate rate = new FxRate(new FxRateId(1L), to, FxView.MEP, new BigDecimal("1200.00"), new BigDecimal("1200.00"), FxRateSource.IOL_SYNTHETIC, null);

        when(fxRateRepository.findByDateBetween(from, to, FxView.MEP)).thenReturn(List.of(rate));

        List<FxRate> result = getFxRatesUseCase.execute(new GetFxRatesCommand(from, to, FxView.MEP));

        assertThat(result).containsExactly(rate);
        verify(fxRateRepository).findByDateBetween(from, to, FxView.MEP);
    }

    @Test
    void getLatestFxRates_returnsLatestForAvailableViews() {
        FxRate mep = new FxRate(new FxRateId(1L), LocalDate.now(), FxView.MEP, new BigDecimal("1200.00"), new BigDecimal("1200.00"), FxRateSource.IOL_SYNTHETIC, null);
        FxRate ccl = new FxRate(new FxRateId(2L), LocalDate.now(), FxView.CCL, new BigDecimal("1250.00"), new BigDecimal("1250.00"), FxRateSource.IOL_SYNTHETIC, null);

        when(fxRateRepository.findLatest(FxView.MEP)).thenReturn(Optional.of(mep));
        when(fxRateRepository.findLatest(FxView.CCL)).thenReturn(Optional.of(ccl));
        when(fxRateRepository.findLatest(FxView.OFICIAL)).thenReturn(Optional.empty());

        List<FxRate> result = getLatestFxRatesUseCase.execute();

        assertThat(result).containsExactlyInAnyOrder(mep, ccl);
    }

    @Test
    void getFxRatesAtDate_callsDerivationAndNeverTouchesRepository() {
        LocalDate date = LocalDate.now().minusDays(1);
        FxSnapshotRates snapshot = new FxSnapshotRates(new BigDecimal("1200.00"), new BigDecimal("1250.00"), new BigDecimal("950.00"), date);

        when(fxRateDerivation.deriveRates(date)).thenReturn(snapshot);

        FxSnapshotRates result = getFxRatesAtDateUseCase.execute(new GetFxRatesAtDateCommand(date));

        assertThat(result).isEqualTo(snapshot);
        verify(fxRateDerivation).deriveRates(date);
        verifyNoInteractions(fxRateRepository);
    }

    @Test
    void getFxRatesAtDate_futureDate_throwsException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> getFxRatesAtDateUseCase.execute(new GetFxRatesAtDateCommand(futureDate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Future dates are not allowed");

        verifyNoInteractions(fxRateDerivation);
        verifyNoInteractions(fxRateRepository);
    }
}
