package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.market.SyncMarketQuotesUseCase;
import com.financialapp.investments.domain.usecase.price.EvaluateThresholdsUseCase;
import com.financialapp.investments.domain.usecase.price.RefreshPricesUseCase;
import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchedulerTest {

    @Test
    void marketDiscoveryScheduler_delegates() {
        SyncMarketQuotesUseCase uc = Mockito.mock(SyncMarketQuotesUseCase.class);
        new MarketDiscoveryScheduler(uc).syncMarketQuotes();
        verify(uc).execute();
    }

    @Test
    void portfolioSnapshotScheduler_delegates() {
        CapturePortfolioSnapshotUseCase uc = Mockito.mock(CapturePortfolioSnapshotUseCase.class);
        when(uc.execute()).thenReturn(new SnapshotCaptureResult(2, 0));
        new PortfolioSnapshotScheduler(uc).captureSnapshots();
        verify(uc).execute();
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void portfolioSnapshotScheduler_logsAnErrorWithTheFailedCount(CapturedOutput output) {
        CapturePortfolioSnapshotUseCase uc = Mockito.mock(CapturePortfolioSnapshotUseCase.class);
        when(uc.execute()).thenReturn(new SnapshotCaptureResult(3, 2));

        new PortfolioSnapshotScheduler(uc).captureSnapshots();

        assertThat(output).contains("ERROR").contains("Portfolio snapshot capture failed for 2 of 3 users");
    }

    @Test
    void priceRefreshScheduler_delegates_inOrder() {
        RefreshPricesUseCase refresh = Mockito.mock(RefreshPricesUseCase.class);
        EvaluateThresholdsUseCase evaluate = Mockito.mock(EvaluateThresholdsUseCase.class);
        new PriceRefreshScheduler(refresh, evaluate).refreshPrices();
        verify(refresh).execute();
        verify(evaluate).execute();
    }
}
