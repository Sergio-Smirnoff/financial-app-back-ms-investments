package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.model.fx.*;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.service.FxRateDerivation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FxRateSyncSchedulerTest {

    @Mock
    private FxRateDerivation derivation;

    @Mock
    private FxRateRepository fxRateRepository;

    @InjectMocks
    private FxRateSyncScheduler scheduler;

    @Test
    void syncFxRates_derivesAndSavesRates() {
        LocalDate today = LocalDate.now();
        FxRate mep = new FxRate(null, today, FxView.MEP, new BigDecimal("1350"), new BigDecimal("1350"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());
        FxRate ccl = new FxRate(null, today, FxView.CCL, new BigDecimal("1400"), new BigDecimal("1400"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());

        given(derivation.deriveAsFxRates(any(LocalDate.class))).willReturn(List.of(mep, ccl));

        scheduler.syncFxRates();

        verify(derivation).deriveAsFxRates(any(LocalDate.class));
        verify(fxRateRepository, times(2)).save(any(FxRate.class));
    }
}
