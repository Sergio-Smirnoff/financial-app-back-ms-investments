package com.financialapp.investments.application.market;

import com.financialapp.investments.application.market.impl.GetMarketPanelUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxRateId;
import com.financialapp.investments.domain.model.fx.FxRateSource;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.MarketIndexRepository;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.domain.usecase.fx.GetLatestFxRates;
import com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase.MarketPanelResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetMarketPanelUseCaseImplTest {

    private MarketQuoteRepository marketQuoteRepository;
    private MarketIndexRepository marketIndexRepository;
    private GetLatestFxRates getLatestFxRates;
    private GetMarketPanelUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        marketQuoteRepository = mock(MarketQuoteRepository.class);
        marketIndexRepository = mock(MarketIndexRepository.class);
        getLatestFxRates = mock(GetLatestFxRates.class);
        useCase = new GetMarketPanelUseCaseImpl(marketQuoteRepository, marketIndexRepository, getLatestFxRates);
    }

    @Test
    void execute_returnsQuotesIndicesAndFxRates() {
        MarketQuote quote = new MarketQuote(new Ticker("GGAL"), Money.of(new BigDecimal("4500.00"), "ARS"), new BigDecimal("2.50"), new BigDecimal("100000"), LocalDateTime.now());
        MarketIndex index = new MarketIndex("MERVAL", new BigDecimal("1834520.00"), new BigDecimal("1.20"), LocalDateTime.now());
        FxRate fxRate = new FxRate(new FxRateId(1L), LocalDate.now(), FxView.MEP, new BigDecimal("1200.00"), new BigDecimal("1200.00"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());

        when(marketQuoteRepository.findAll()).thenReturn(List.of(quote));
        when(marketIndexRepository.findAll()).thenReturn(List.of(index));
        when(getLatestFxRates.execute()).thenReturn(List.of(fxRate));

        MarketPanelResult result = useCase.execute();

        assertThat(result.quotes()).containsExactly(quote);
        assertThat(result.indices()).containsExactly(index);
        assertThat(result.fxRates()).containsExactly(fxRate);
    }
}
