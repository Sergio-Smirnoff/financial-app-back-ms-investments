package com.financialapp.investments.application.market;

import com.financialapp.investments.application.market.impl.GetTickerResearchUseCaseImpl;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.PriceRange;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.usecase.market.command.GetTickerResearchCommand;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTickerResearchUseCaseImplTest {

    @Mock private IolGateway iolGateway;
    @InjectMocks private GetTickerResearchUseCaseImpl useCase;

    @Test
    void returns_quote_and_series() {
        Ticker ticker = new Ticker("GGAL");

        PriceDetail quote = new PriceDetail(
                new BigDecimal("1234.50"),
                new BigDecimal("1200.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("1190.00"),
                new BigDecimal("500000"),
                new BigDecimal("2.85"),
                "ARS"
        );

        HistoricalPricePoint point = new HistoricalPricePoint(
                new BigDecimal("1234.50"),
                new BigDecimal("1200.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("1190.00"),
                new BigDecimal("500000"),
                new BigDecimal("2.85"),
                "ARS",
                LocalDateTime.of(2026, 6, 12, 15, 0)
        );

        when(iolGateway.getPrice(any(), any())).thenReturn(Optional.of(quote));
        when(iolGateway.getHistoricalSeries(any(), any(), any(), any())).thenReturn(List.of(point));

        TickerResearchResult result = useCase.execute(
                new GetTickerResearchCommand(ticker, AssetType.STOCK, PriceRange.D30));

        assertThat(result.ticker()).isEqualTo(ticker);
        assertThat(result.currentQuote()).isPresent();
        assertThat(result.currentQuote().get()).isEqualTo(quote);
        assertThat(result.series()).hasSize(1);
        assertThat(result.series().get(0)).isEqualTo(point);
    }

    @Test
    void returns_empty_quote_when_iol_has_no_price() {
        when(iolGateway.getPrice(any(), any())).thenReturn(Optional.empty());
        when(iolGateway.getHistoricalSeries(any(), any(), any(), any())).thenReturn(List.of());

        TickerResearchResult result = useCase.execute(
                new GetTickerResearchCommand(new Ticker("YPFD"), AssetType.STOCK, PriceRange.D90));

        assertThat(result.currentQuote()).isEmpty();
        assertThat(result.series()).isEmpty();
    }
}
