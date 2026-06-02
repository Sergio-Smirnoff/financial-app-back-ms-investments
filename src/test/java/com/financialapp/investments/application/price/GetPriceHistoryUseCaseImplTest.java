package com.financialapp.investments.application.price;

import com.financialapp.investments.domain.common.model.Cbu;

import com.financialapp.investments.application.price.impl.GetPriceHistoryUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceHistoryRepository;
import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import com.financialapp.investments.domain.exception.IolServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPriceHistoryUseCaseImplTest {

    @Mock private AssetPriceHistoryRepository historyRepository;
    @Mock private HoldingQueryGateway holdingQueryGateway;
    @Mock private IolGateway iolGateway;
    @InjectMocks private GetPriceHistoryUseCaseImpl useCase;

    private static final Ticker TIC = new Ticker("AAPL");
    private static final LocalDateTime FROM = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 1, 10, 0, 0);

    @Test
    void execute_enoughHistory_skipsBackfill() {
        when(historyRepository.countByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(10L);
        when(historyRepository.findByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(List.of());

        useCase.execute(new GetPriceHistoryCommand(TIC, FROM, TO));

        verify(iolGateway, never()).getHistoricalSeries(any(), any(), any(), any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void execute_lowHistory_backfills_filtersExisting_andSavesNew() {
        when(historyRepository.countByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(0L);
        when(holdingQueryGateway.findFirstByTicker(TIC)).thenReturn(Optional.of(
                holdingWithType(AssetType.CEDEAR)));
        HistoricalPricePoint p1 = point(LocalDateTime.of(2026, 1, 1, 0, 0));
        HistoricalPricePoint p2 = point(LocalDateTime.of(2026, 1, 2, 0, 0));
        when(iolGateway.getHistoricalSeries(eq(TIC), eq(AssetType.CEDEAR),
                eq(FROM.toLocalDate()), eq(TO.toLocalDate())))
                .thenReturn(List.of(p1, p2));
        when(historyRepository.existsByTickerAndPricedAt(TIC, p1.pricedAt())).thenReturn(true);
        when(historyRepository.existsByTickerAndPricedAt(TIC, p2.pricedAt())).thenReturn(false);
        when(historyRepository.findByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(List.of());

        useCase.execute(new GetPriceHistoryCommand(TIC, FROM, TO));

        verify(historyRepository, times(1)).save(any(AssetPriceHistory.class));
    }

    @Test
    void execute_noOwningHolding_defaultsAssetTypeStock() {
        when(historyRepository.countByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(0L);
        when(holdingQueryGateway.findFirstByTicker(TIC)).thenReturn(Optional.empty());
        when(iolGateway.getHistoricalSeries(eq(TIC), eq(AssetType.STOCK), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(historyRepository.findByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(List.of());

        useCase.execute(new GetPriceHistoryCommand(TIC, FROM, TO));

        verify(iolGateway).getHistoricalSeries(eq(TIC), eq(AssetType.STOCK), any(), any());
    }

    @Test
    void execute_iolFailure_wrappedAsIolServiceException() {
        when(historyRepository.countByTickerAndPricedAtBetween(TIC, FROM, TO)).thenReturn(0L);
        when(holdingQueryGateway.findFirstByTicker(TIC)).thenReturn(Optional.empty());
        when(iolGateway.getHistoricalSeries(any(), any(), any(), any()))
                .thenThrow(new IolServiceException("iol down"));

        assertThatThrownBy(() -> useCase.execute(new GetPriceHistoryCommand(TIC, FROM, TO)))
                .isInstanceOf(IolServiceException.class);
    }

    private static HistoricalPricePoint point(LocalDateTime at) {
        BigDecimal one = BigDecimal.ONE;
        return new HistoricalPricePoint(one, one, one, one, one, one, "ARS", at);
    }

    private static Holding holdingWithType(AssetType type) {
        return new Holding(new HoldingId(1L), new UserId(1L), new Cbu("0070009000000000000017"),
                TIC, "n", type, new HoldingQuantity(BigDecimal.ONE),
                Money.of(BigDecimal.ONE, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
