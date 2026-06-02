package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.market.SyncMarketQuotesUseCase;
import com.financialapp.investments.domain.usecase.price.EvaluateThresholdsUseCase;
import com.financialapp.investments.domain.usecase.price.RefreshPricesUseCase;
import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

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
        new PortfolioSnapshotScheduler(uc).captureSnapshots();
        verify(uc).execute();
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
