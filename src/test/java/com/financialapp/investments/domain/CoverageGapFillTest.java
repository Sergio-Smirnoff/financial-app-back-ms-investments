package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.BankNumber;

import com.financialapp.investments.application.portfolio.impl.GetPortfolioSummaryUseCaseImpl;
import com.financialapp.investments.application.price.impl.EvaluateThresholdsUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoverageGapFillTest {

    @Test
    void ticker_toString_returnsValue() {
        assertThat(new Ticker("AAPL").toString()).isEqualTo("AAPL");
    }

    @Test
    void ticker_lowercaseValue_isUppercased() {
        assertThat(new Ticker("aapl").value()).isEqualTo("AAPL");
    }

    // -------- EvaluateThresholdsUseCaseImpl extra branches --------

    private static class EvalFixture {
        @Mock HoldingQueryGateway holdingQueryGateway;
        @Mock HoldingRepository holdingRepository;
        @Mock AssetPriceRepository assetPriceRepository;
        @Mock DomainEventPublisher eventPublisher;
        @InjectMocks EvaluateThresholdsUseCaseImpl useCase;
    }

    @ExtendWith(MockitoExtension.class)
    static class EvaluateThresholdsCoverage {
        @Mock HoldingQueryGateway holdingQueryGateway;
        @Mock HoldingRepository holdingRepository;
        @Mock AssetPriceRepository assetPriceRepository;
        @Mock DomainEventPublisher eventPublisher;
        @InjectMocks EvaluateThresholdsUseCaseImpl useCase;

        @Test
        void zeroAvgPrice_skipsHolding() {
            Holding h = holding(BigDecimal.ZERO, new ThresholdConfig(new BigDecimal("10"), null),
                    NotificationTimestamps.empty());
            when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(h));
            when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                    .thenReturn(List.of(price("AAPL", new BigDecimal("100"))));
            useCase.execute();
            verifyNoInteractions(eventPublisher);
            verify(holdingRepository, never()).saveAll(any());
        }

        @Test
        void lossCooldown_recentlyNotified_skips() {
            Holding h = holding(new BigDecimal("100"),
                    new ThresholdConfig(null, new BigDecimal("10")),
                    new NotificationTimestamps(null, LocalDateTime.now().minusHours(1)));
            when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(h));
            when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                    .thenReturn(List.of(price("AAPL", new BigDecimal("80"))));
            useCase.execute();
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void lossCooldown_longAgo_publishes() {
            Holding h = holding(new BigDecimal("100"),
                    new ThresholdConfig(null, new BigDecimal("10")),
                    new NotificationTimestamps(null, LocalDateTime.now().minusHours(25)));
            when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(h));
            when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                    .thenReturn(List.of(price("AAPL", new BigDecimal("80"))));
            useCase.execute();
            verify(eventPublisher).publish(any(PriceThresholdBreachedEvent.class));
            verify(holdingRepository).saveAll(any());
        }

        @Test
        void bothGainAndLossConfigured_onlyGainBreached_publishesGain() {
            Holding h = holding(new BigDecimal("100"),
                    new ThresholdConfig(new BigDecimal("10"), new BigDecimal("10")),
                    NotificationTimestamps.empty());
            when(holdingQueryGateway.findWithThresholds()).thenReturn(List.of(h));
            when(assetPriceRepository.findAllByTickerIn(any(Set.class)))
                    .thenReturn(List.of(price("AAPL", new BigDecimal("115"))));
            useCase.execute();
            verify(eventPublisher, times(1)).publish(any(PriceThresholdBreachedEvent.class));
        }

        private static Holding holding(BigDecimal avg, ThresholdConfig tc, NotificationTimestamps ts) {
            return new Holding(new HoldingId(1L), new UserId(1L),
                    new BankNumber("007"),
                    new Ticker("AAPL"), "n", AssetType.STOCK,
                    new HoldingQuantity(new BigDecimal("10")),
                    new Money(avg, java.util.Currency.getInstance("ARS")),
                    tc, ts, LocalDateTime.now(), LocalDateTime.now());
        }

        private static AssetPrice price(String t, BigDecimal p) {
            return new AssetPrice(new AssetPriceId(1L), new Ticker(t), AssetType.STOCK,
                    p, "ARS", null, null, null, null, null,
                    LocalDateTime.now(), LocalDateTime.now());
        }
    }

    // -------- GetPortfolioSummaryUseCaseImpl extra branches --------

    @ExtendWith(MockitoExtension.class)
    static class GetPortfolioSummaryCoverage {
        @Mock GetHoldingsWithPricesUseCase getHoldingsWithPricesUseCase;
        @InjectMocks GetPortfolioSummaryUseCaseImpl useCase;

        @Test
        void zeroCostBasis_plPercentIsZero_andBreakdownPercentZero() {
            Holding h = new Holding(new HoldingId(1L), new UserId(1L),
                    new BankNumber("007"),
                    new Ticker("AAPL"), "n", AssetType.STOCK,
                    new HoldingQuantity(BigDecimal.ONE),
                    Money.of(BigDecimal.ZERO, "ARS"),
                    ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                    LocalDateTime.now(), LocalDateTime.now());
            HoldingWithPriceResult r = new HoldingWithPriceResult(h, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of(r));

            PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(new UserId(1L)));
            assertThat(result.byCurrency()).hasSize(1);
            assertThat(result.byCurrency().get(0).plPercent()).isEqualByComparingTo("0");
            assertThat(result.byCurrency().get(0).breakdown()).hasSize(1);
            assertThat(result.byCurrency().get(0).breakdown().get(0).percentage()).isEqualByComparingTo("0");
        }
    }
}
