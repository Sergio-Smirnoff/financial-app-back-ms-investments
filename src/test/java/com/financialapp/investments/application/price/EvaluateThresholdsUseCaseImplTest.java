package com.financialapp.investments.application.price;

import com.financialapp.investments.domain.common.model.Cbu;

import com.financialapp.investments.application.price.impl.EvaluateThresholdsUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateThresholdsUseCaseImplTest {

    @Mock
    private HoldingQueryGateway holdingQueryGateway;
    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private AssetPriceRepository assetPriceRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private EvaluateThresholdsUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);

    @Test
    void gainThreshold_breached_publishesEvent() {
        Holding holding = holdingWithGainThreshold("AAPL", new BigDecimal("100"), new BigDecimal("10"));
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("115")))); // +15% > 10%

        useCase.execute();

        ArgumentCaptor<PriceThresholdBreachedEvent> captor =
                ArgumentCaptor.forClass(PriceThresholdBreachedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        PriceThresholdBreachedEvent event = captor.getValue();
        assertThat(event.direction()).isEqualTo(Direction.GAIN);
        assertThat(event.ticker().value()).isEqualTo("AAPL");
        verify(holdingRepository).saveAll(any());
    }

    @Test
    void gainThreshold_notBreached_noEvent() {
        Holding holding = holdingWithGainThreshold("AAPL", new BigDecimal("100"), new BigDecimal("20"));
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("105")))); // +5% < 20%

        useCase.execute();

        verifyNoInteractions(eventPublisher);
        verify(holdingRepository, never()).saveAll(any());
    }

    @Test
    void lossThreshold_breached_publishesEvent() {
        Holding holding = holdingWithLossThreshold("VALE3", new BigDecimal("100"), new BigDecimal("10"));
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                .thenReturn(List.of(assetPrice("VALE3", new BigDecimal("88")))); // -12% > 10% loss

        useCase.execute();

        ArgumentCaptor<PriceThresholdBreachedEvent> captor =
                ArgumentCaptor.forClass(PriceThresholdBreachedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().direction()).isEqualTo(Direction.LOSS);
    }

    @Test
    void cooldown_preventsRepeatNotification() {
        LocalDateTime recentlyNotified = LocalDateTime.now().minusHours(1);
        Holding holding = new Holding(new HoldingId(1L), USER_ID, new Cbu("0070009000000000000017"),
                new Ticker("AAPL"), "Test", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("100"), "ARS"),
                new ThresholdConfig(new BigDecimal("10"), null),
                new NotificationTimestamps(recentlyNotified, null),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("115"))));

        useCase.execute();

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void cooldown_allows_notificationAfter24Hours() {
        LocalDateTime longAgo = LocalDateTime.now().minusHours(25);
        Holding holding = new Holding(new HoldingId(1L), USER_ID, new Cbu("0070009000000000000017"),
                new Ticker("AAPL"), "Test", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("100"), "ARS"),
                new ThresholdConfig(new BigDecimal("10"), null),
                new NotificationTimestamps(longAgo, null),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("115"))));

        useCase.execute();

        verify(eventPublisher).publish(any(PriceThresholdBreachedEvent.class));
    }

    @Test
    void noPrice_skipsHolding() {
        Holding holding = holdingWithGainThreshold("AAPL", new BigDecimal("100"), new BigDecimal("10"));
        when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(holding));
        when(assetPriceRepository.findAllByTickerIn(any(Set.class))).thenReturn(List.of());

        useCase.execute();

        verifyNoInteractions(eventPublisher);
    }

    private static Holding holdingWithGainThreshold(String ticker, BigDecimal avgPrice, BigDecimal gainPct) {
        return new Holding(new HoldingId(1L), USER_ID, new Cbu("0070009000000000000017"),
                new Ticker(ticker), "Test", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(avgPrice, "ARS"),
                new ThresholdConfig(gainPct, null),
                NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static Holding holdingWithLossThreshold(String ticker, BigDecimal avgPrice, BigDecimal lossPct) {
        return new Holding(new HoldingId(2L), USER_ID, new Cbu("0070009000000000000017"),
                new Ticker(ticker), "Test", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(avgPrice, "ARS"),
                new ThresholdConfig(null, lossPct),
                NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static AssetPrice assetPrice(String ticker, BigDecimal price) {
        return new AssetPrice(new AssetPriceId(1L), new Ticker(ticker), AssetType.STOCK,
                price, "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
