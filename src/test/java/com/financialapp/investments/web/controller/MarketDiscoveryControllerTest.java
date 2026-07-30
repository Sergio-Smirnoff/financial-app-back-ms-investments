package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxRateId;
import com.financialapp.investments.domain.model.fx.FxRateSource;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.usecase.market.GetMarketDiscoveryUseCase;
import com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase;
import com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase.MarketPanelResult;
import com.financialapp.investments.domain.usecase.market.GetTickerResearchUseCase;
import com.financialapp.investments.domain.usecase.market.SearchTickersUseCase;
import com.financialapp.investments.web.mapper.MarketWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketDiscoveryController.class)
@Import(MarketWebMapper.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class MarketDiscoveryControllerTest {

    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMarketDiscoveryUseCase getMarketDiscoveryUseCase;

    @MockBean
    private SearchTickersUseCase searchTickersUseCase;

    @MockBean
    private GetTickerResearchUseCase getTickerResearchUseCase;

    @MockBean
    private GetMarketPanelUseCase getMarketPanelUseCase;

    @Test
    void getPanel_returnsQuotesIndicesAndFxRatesInEnvelope() throws Exception {
        MarketQuote quote = new MarketQuote(new Ticker("GGAL"), Money.of(new BigDecimal("4500.00"), "ARS"), new BigDecimal("2.50"), new BigDecimal("100000"), LocalDateTime.now());
        MarketIndex index = new MarketIndex("MERVAL", new BigDecimal("1834520.00"), new BigDecimal("1.20"), LocalDateTime.now());
        FxRate fxRate = new FxRate(new FxRateId(1L), LocalDate.of(2026, 7, 28), FxView.MEP, new BigDecimal("1200.00"), new BigDecimal("1200.00"), FxRateSource.IOL_SYNTHETIC, LocalDateTime.now());

        when(getMarketPanelUseCase.execute()).thenReturn(new MarketPanelResult(List.of(quote), List.of(index), List.of(fxRate)));

        mockMvc.perform(get("/api/v1/investments/market/panel")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.quotes[0].ticker").value("GGAL"))
                .andExpect(jsonPath("$.data.indices[0].code").value("MERVAL"))
                .andExpect(jsonPath("$.data.fxRates[0].view").value("MEP"));
    }
}
