package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.Cbu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.*;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.exception.holding.HoldingQuantityNonPositiveException;
import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.web.dto.request.HoldingRequest;
import com.financialapp.investments.web.mapper.HoldingWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.financialapp.investments.domain.common.model.PageResult;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HoldingController.class)
@Import(HoldingWebMapper.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class HoldingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreateHoldingUseCase createHoldingUseCase;
    @MockBean UpdateHoldingUseCase updateHoldingUseCase;
    @MockBean CloseHoldingUseCase closeHoldingUseCase;
    @MockBean ListHoldingsUseCase listHoldingsUseCase;
    @MockBean GetAccountValuationUseCase getAccountValuationUseCase;
    @MockBean SupportedCurrencies supportedCurrencies;

    private static final String TOKEN = "test-token";
    private static final Long USER_ID = 1L;
    private static final String BASE_URL = "/api/v1/investments/holdings";

    @BeforeEach
    void stubSupportedCurrencies() {
        Set<Currency> allowed = Set.of(Currency.getInstance("ARS"), Currency.getInstance("USD"));
        when(supportedCurrencies.isSupported(any(Currency.class)))
                .thenAnswer(inv -> allowed.contains(inv.getArgument(0)));
        when(supportedCurrencies.all()).thenReturn(allowed);
    }

    // --- auth filter ---

    @Test
    void list_missingToken_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL).header("X-User-Id", USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_wrongToken_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Internal-Token", "wrong-token")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isUnauthorized());
    }

    // --- list ---

    @Test
    void list_validRequest_returns200WithSuccessBody() throws Exception {
        when(listHoldingsUseCase.execute(any())).thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    // --- create ---

    @Test
    void create_validRequest_returns201WithHoldingData() throws Exception {
        when(createHoldingUseCase.execute(any(CreateHoldingCommand.class))).thenReturn(sampleHolding());

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.ticker").value("AAPL"))
                .andExpect(jsonPath("$.data.assetType").value("STOCK"));
    }

    @Test
    void create_invalidBody_returns400WithErrorResponse() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HoldingRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void create_useCaseThrowsQuantityError_returns422WithErrorResponse() throws Exception {
        when(createHoldingUseCase.execute(any())).thenThrow(new HoldingQuantityNonPositiveException());

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("holding_quantity_invalid"));
    }

    // --- delete ---

    @Test
    void delete_validRequest_returns200() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void delete_holdingNotFound_returns404WithErrorResponse() throws Exception {
        doThrow(new ResourceNotFoundException("Holding not found with id: 99"))
                .when(closeHoldingUseCase).execute(any());

        mockMvc.perform(delete(BASE_URL + "/99")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("resource_not_found"))
                .andExpect(jsonPath("$.message").value("Holding not found with id: 99"));
    }

    // --- helpers ---

    private Holding sampleHolding() {
        return new Holding(
                new HoldingId(1L),
                new UserId(USER_ID),
                new Cbu("0070009000000000000017"),
                new Ticker("AAPL"),
                "Apple Inc",
                AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("100.00"), "USD"),
                null, NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private HoldingRequest validRequest() {
        HoldingRequest req = new HoldingRequest();
        req.setAccountCbu("0070009000000000000017");
        req.setTicker("AAPL");
        req.setName("Apple Inc");
        req.setAssetType("STOCK");
        req.setQuantity(new BigDecimal("10"));
        req.setAvgPurchasePrice(new BigDecimal("100.00"));
        req.setCurrency("USD");
        return req;
    }
}
