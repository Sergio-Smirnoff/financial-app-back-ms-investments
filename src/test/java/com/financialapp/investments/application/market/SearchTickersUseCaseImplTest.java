package com.financialapp.investments.application.market;

import com.financialapp.investments.application.market.impl.SearchTickersUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.domain.usecase.market.response.TickerSearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class SearchTickersUseCaseImplTest {

    @Mock private MarketQuoteRepository marketQuoteRepository;
    @InjectMocks private SearchTickersUseCaseImpl useCase;

    @Test
    void blank_query_returns_empty_list_without_calling_repository() {
        List<TickerSearchResult> result = useCase.execute("   ");

        assertThat(result).isEmpty();
        verify(marketQuoteRepository, never()).search(anyString());
    }

    @Test
    void null_query_returns_empty_list_without_calling_repository() {
        List<TickerSearchResult> result = useCase.execute(null);

        assertThat(result).isEmpty();
        verify(marketQuoteRepository, never()).search(anyString());
    }

    @Test
    void non_blank_query_trims_and_delegates_to_repository() {
        Ticker ticker = new Ticker("GGAL");
        Money price = Money.of(BigDecimal.valueOf(1500), "ARS");
        BigDecimal variation = new BigDecimal("2.50");
        when(marketQuoteRepository.search("ggal")).thenReturn(List.of(
                new MarketQuote(ticker, price, variation, BigDecimal.ONE, LocalDateTime.now())));

        List<TickerSearchResult> result = useCase.execute("  ggal ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ticker()).isEqualTo(ticker);
        assertThat(result.get(0).price()).isEqualTo(price);
        assertThat(result.get(0).variation()).isEqualByComparingTo(variation);
        verify(marketQuoteRepository).search("ggal");
    }
}
