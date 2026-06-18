package com.financialapp.investments.application.market;

import com.financialapp.investments.domain.common.model.BankNumber;

import com.financialapp.investments.application.market.impl.GetMarketDiscoveryUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.response.MarketDiscoveryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMarketDiscoveryUseCaseImplTest {

    @Mock private MarketQuoteRepository marketQuoteRepository;
    @Mock private HoldingRepository holdingRepository;
    @InjectMocks private GetMarketDiscoveryUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);

    @Test
    void execute_excludesOwned_sortsByAbsVariationDesc_appliesLimit() {
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of(
                holdingWithTicker("AAPL")));
        when(marketQuoteRepository.findAll()).thenReturn(List.of(
                quote("AAPL", new BigDecimal("99")), // owned, excluded
                quote("GOOG", new BigDecimal("-15")),
                quote("MSFT", new BigDecimal("5")),
                quote("AMZN", new BigDecimal("20"))));

        MarketDiscoveryResult r = useCase.execute(new GetMarketDiscoveryCommand(USER, 2));

        assertThat(r.marketDataAvailable()).isTrue();
        assertThat(r.opportunities()).extracting(x -> x.ticker().value()).containsExactly("AMZN", "GOOG");
    }

    @Test
    void execute_handlesNullVariation_asZero() {
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of());
        when(marketQuoteRepository.findAll()).thenReturn(List.of(
                quote("X", null),
                quote("Y", new BigDecimal("5"))));

        MarketDiscoveryResult r = useCase.execute(new GetMarketDiscoveryCommand(USER, 10));

        assertThat(r.marketDataAvailable()).isTrue();
        assertThat(r.opportunities()).extracting(x -> x.ticker().value()).containsExactly("Y", "X");
    }

    @Test
    void reportsUnavailableWhenCacheEmpty() {
        when(holdingRepository.findByUserId(any())).thenReturn(List.of());
        when(marketQuoteRepository.findAll()).thenReturn(List.of());
        MarketDiscoveryResult result = useCase.execute(new GetMarketDiscoveryCommand(new UserId(1L), 5));
        assertThat(result.marketDataAvailable()).isFalse();
        assertThat(result.opportunities()).isEmpty();
    }

    @Test
    void reportsAvailableWhenCacheHasQuotes() {
        when(holdingRepository.findByUserId(any())).thenReturn(List.of());
        when(marketQuoteRepository.findAll()).thenReturn(List.of(quote("GGAL", new BigDecimal("3.5"))));
        MarketDiscoveryResult result = useCase.execute(new GetMarketDiscoveryCommand(new UserId(1L), 5));
        assertThat(result.marketDataAvailable()).isTrue();
        assertThat(result.opportunities()).hasSize(1);
    }

    @Test
    void reportsAvailableWithEmptyOpportunitiesWhenUserOwnsAllCachedTickers() {
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of(
                holdingWithTicker("GGAL")));
        when(marketQuoteRepository.findAll()).thenReturn(List.of(
                quote("GGAL", new BigDecimal("3.5"))));
        MarketDiscoveryResult result = useCase.execute(new GetMarketDiscoveryCommand(USER, 5));
        assertThat(result.marketDataAvailable()).isTrue();
        assertThat(result.opportunities()).isEmpty();
    }

    private static Holding holdingWithTicker(String t) {
        return new Holding(new HoldingId(1L), USER, new BankNumber("007"),
                new Ticker(t), "n", AssetType.STOCK,
                new HoldingQuantity(BigDecimal.ONE), Money.of(BigDecimal.ONE, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static MarketQuote quote(String t, BigDecimal variation) {
        return new MarketQuote(new Ticker(t), Money.of(BigDecimal.ONE, "ARS"),
                variation, BigDecimal.ONE, LocalDateTime.now());
    }
}
