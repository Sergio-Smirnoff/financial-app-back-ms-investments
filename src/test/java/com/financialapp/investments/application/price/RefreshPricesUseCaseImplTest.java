package com.financialapp.investments.application.price;

import com.financialapp.investments.domain.common.model.Cbu;

import com.financialapp.investments.application.price.impl.RefreshPricesUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import com.financialapp.investments.domain.repository.AssetPriceHistoryRepository;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.RefreshJobRepository;
import com.financialapp.investments.domain.exception.IolServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshPricesUseCaseImplTest {

    @Mock private HoldingQueryGateway holdingQueryGateway;
    @Mock private AssetPriceRepository assetPriceRepository;
    @Mock private AssetPriceHistoryRepository assetPriceHistoryRepository;
    @Mock private RefreshJobRepository refreshJobRepository;
    @Mock private IolGateway iolGateway;
    @InjectMocks private RefreshPricesUseCaseImpl useCase;

    @Test
    void execute_noTickers_doesNothing() {
        when(holdingQueryGateway.findDistinctTickers()).thenReturn(List.of());
        useCase.execute();
        verify(refreshJobRepository, never()).save(any());
    }

    @Test
    void execute_priceMissing_skipsTicker() {
        when(holdingQueryGateway.findDistinctTickers()).thenReturn(List.of(new Ticker("AAPL")));
        when(refreshJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(holdingQueryGateway.findFirstByTicker(any())).thenReturn(Optional.empty());
        when(iolGateway.getPrice(any(), any())).thenReturn(Optional.empty());

        useCase.execute();

        verify(assetPriceRepository, never()).save(any());
        verify(assetPriceHistoryRepository, never()).save(any());
    }

    @Test
    void execute_priceFound_savesPriceAndHistory_andAdvances_andCompletes() {
        when(holdingQueryGateway.findDistinctTickers()).thenReturn(List.of(new Ticker("AAPL")));
        when(refreshJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(holdingQueryGateway.findFirstByTicker(any())).thenReturn(Optional.of(holding(AssetType.STOCK)));
        when(iolGateway.getPrice(any(), any())).thenReturn(Optional.of(detail()));
        when(assetPriceRepository.findByTicker(any())).thenReturn(Optional.empty());

        useCase.execute();

        verify(assetPriceRepository).save(any(AssetPrice.class));
        verify(assetPriceHistoryRepository).save(any(AssetPriceHistory.class));
        ArgumentCaptor<RefreshJob> jobCap = ArgumentCaptor.forClass(RefreshJob.class);
        verify(refreshJobRepository, atLeastOnce()).save(jobCap.capture());
        assertThat(jobCap.getAllValues())
                .extracting(RefreshJob::status)
                .contains(RefreshJobStatus.COMPLETED);
    }

    @Test
    void execute_existingAssetPrice_reusesId() {
        when(holdingQueryGateway.findDistinctTickers()).thenReturn(List.of(new Ticker("AAPL")));
        when(refreshJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(holdingQueryGateway.findFirstByTicker(any())).thenReturn(Optional.of(holding(AssetType.STOCK)));
        when(iolGateway.getPrice(any(), any())).thenReturn(Optional.of(detail()));
        AssetPrice existing = new AssetPrice(new AssetPriceId(77L), new Ticker("AAPL"), AssetType.STOCK,
                BigDecimal.ONE, "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
        when(assetPriceRepository.findByTicker(any())).thenReturn(Optional.of(existing));

        useCase.execute();

        ArgumentCaptor<AssetPrice> apCap = ArgumentCaptor.forClass(AssetPrice.class);
        verify(assetPriceRepository).save(apCap.capture());
        assertThat(apCap.getValue().id().value()).isEqualTo(77L);
    }

    @Test
    void execute_iolFailure_interruptsJob_throws() {
        when(holdingQueryGateway.findDistinctTickers()).thenReturn(List.of(new Ticker("AAPL")));
        when(refreshJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(holdingQueryGateway.findFirstByTicker(any())).thenReturn(Optional.empty());
        when(iolGateway.getPrice(any(), any())).thenThrow(new IolServiceException("iol fail"));

        assertThatThrownBy(() -> useCase.execute()).isInstanceOf(IolServiceException.class);

        ArgumentCaptor<RefreshJob> cap = ArgumentCaptor.forClass(RefreshJob.class);
        verify(refreshJobRepository, atLeastOnce()).save(cap.capture());
        assertThat(cap.getAllValues())
                .extracting(RefreshJob::status)
                .contains(RefreshJobStatus.INTERRUPTED);
    }

    private static Holding holding(AssetType type) {
        return new Holding(new HoldingId(1L), new UserId(1L), new Cbu("0070009000000000000017"),
                new Ticker("AAPL"), "n", type,
                new HoldingQuantity(BigDecimal.ONE), Money.of(BigDecimal.ONE, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static PriceDetail detail() {
        BigDecimal one = BigDecimal.ONE;
        return new PriceDetail(one, one, one, one, one, one, "ARS");
    }
}
