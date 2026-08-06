package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.portfolio.SearchPositions;
import com.financialapp.investments.web.mapper.PortfolioWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PositionSearchController.class)
@Import(PortfolioWebMapper.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class PositionSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchPositions searchPositions;

    private static final String TOKEN = "test-token";
    private static final Long USER_ID = 3L;
    private static final String BASE_URL = "/api/v1/investments/positions/search";

    @Test
    void search_missingToken_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL).param("q", "ypf").header("X-User-Id", USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void search_validQuery_returns200WithPositions() throws Exception {
        Holding h = new Holding(
                new HoldingId(10L),
                new UserId(USER_ID),
                new BankNumber("007"),
                new Ticker("YPFD"),
                "YPF S.A.",
                AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("5")),
                Money.of(new BigDecimal("1000.00"), "ARS"),
                ThresholdConfig.disabled(),
                NotificationTimestamps.empty(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(searchPositions.execute(eq(new UserId(USER_ID)), eq("ypf")))
                .thenReturn(List.of(h));

        mockMvc.perform(get(BASE_URL)
                        .param("q", "ypf")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].holdingId").value(10))
                .andExpect(jsonPath("$.data[0].ticker").value("YPFD"))
                .andExpect(jsonPath("$.data[0].name").value("YPF S.A."))
                .andExpect(jsonPath("$.data[0].quantity").value("5"))
                .andExpect(jsonPath("$.data[0].marketValue").value("5000.00"))
                .andExpect(jsonPath("$.data[0].currency").value("ARS"));
    }
}
