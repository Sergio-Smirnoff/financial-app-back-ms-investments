package com.financialapp.investments.application.market;

import com.financialapp.investments.application.market.impl.SyncMarketQuotesUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncMarketQuotesUseCaseImplTest {

    @Mock private IolGateway iolGateway;
    @Mock private MarketQuoteRepository marketQuoteRepository;
    @InjectMocks private SyncMarketQuotesUseCaseImpl useCase;

    @Test
    void execute_persistsQuotesFromIol() {
        List<MarketQuote> quotes = List.of(new MarketQuote(new Ticker("X"),
                Money.of(BigDecimal.ONE, "ARS"), BigDecimal.ONE, BigDecimal.ONE, LocalDateTime.now()));
        when(iolGateway.getPanelQuotes("merval")).thenReturn(quotes);

        useCase.execute();

        verify(marketQuoteRepository).saveAll(quotes);
    }

    @Test
    void execute_iolFailure_wrappedAsIolServiceException() {
        when(iolGateway.getPanelQuotes("merval"))
                .thenThrow(new InfrastructureException("iol down"));

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(IolServiceException.class);
        verify(marketQuoteRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
