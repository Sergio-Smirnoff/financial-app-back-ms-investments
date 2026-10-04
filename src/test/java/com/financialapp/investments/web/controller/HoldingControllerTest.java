package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.command.SellHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.response.HoldingSaleResult;
import com.financialapp.investments.domain.usecase.holding.*;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.exception.holding.HoldingQuantityNonPositiveException;
import com.financialapp.investments.domain.exception.holding.HoldingSaleExceedsQuantityException;
import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.web.dto.request.HoldingRequest;
import com.financialapp.investments.web.mapper.HoldingWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @MockBean SellHoldingUseCase sellHoldingUseCase;
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
    void create_aQuantityWithMoreThanSixDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setQuantity(new BigDecimal("1.0000001"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(createHoldingUseCase, never()).execute(any());
    }

    @Test
    void create_anAveragePriceWithThirteenIntegerDigits_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setAvgPurchasePrice(new BigDecimal("1234567890123"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(createHoldingUseCase, never()).execute(any());
    }

    @Test
    void create_valuesAtTheColumnLimit_areAccepted() throws Exception {
        when(createHoldingUseCase.execute(any(CreateHoldingCommand.class))).thenReturn(sampleHolding());
        HoldingRequest request = validRequest();
        request.setQuantity(new BigDecimal("999999999999.999999"));
        request.setAvgPurchasePrice(new BigDecimal("999999999999.999999"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateHoldingCommand> command = ArgumentCaptor.forClass(CreateHoldingCommand.class);
        verify(createHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity().value()).isEqualByComparingTo("999999999999.999999");
        assertThat(command.getValue().avgPurchasePrice().amount()).isEqualByComparingTo("999999999999.999999");
    }

    @Test
    void create_trailingZerosCountTowardTheSixDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setQuantity(new BigDecimal("1.1000000"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(createHoldingUseCase, never()).execute(any());
    }

    @Test
    void update_aQuantityWithMoreThanSixDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setQuantity(new BigDecimal("1.0000001"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(updateHoldingUseCase, never()).execute(any());
    }

    @Test
    void update_trailingZerosCountTowardTheSixDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setQuantity(new BigDecimal("1.1000000"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(updateHoldingUseCase, never()).execute(any());
    }

    @Test
    void update_anAveragePriceWithMoreThanSixDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setAvgPurchasePrice(new BigDecimal("100.0000001"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(updateHoldingUseCase, never()).execute(any());
    }

    @Test
    void create_aGainThresholdOfFourIntegerDigits_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setNotifyGainThresholdPct(new BigDecimal("1000"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(createHoldingUseCase, never()).execute(any());
    }

    @Test
    void create_aLossThresholdWithThreeDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setNotifyLossThresholdPct(new BigDecimal("1.234"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(createHoldingUseCase, never()).execute(any());
    }

    @Test
    void update_aThresholdWithThreeDecimals_returns400() throws Exception {
        HoldingRequest request = validRequest();
        request.setNotifyGainThresholdPct(new BigDecimal("1.234"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(updateHoldingUseCase, never()).execute(any());
    }

    @Test
    void create_thresholdsThatFitTheColumn_areAccepted() throws Exception {
        when(createHoldingUseCase.execute(any(CreateHoldingCommand.class))).thenReturn(sampleHolding());
        HoldingRequest request = validRequest();
        request.setNotifyGainThresholdPct(new BigDecimal("12.5"));
        request.setNotifyLossThresholdPct(new BigDecimal("999.99"));

        mockMvc.perform(post(BASE_URL)
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createHoldingUseCase).execute(any(CreateHoldingCommand.class));
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
                .when(sellHoldingUseCase).execute(any());

        mockMvc.perform(delete(BASE_URL + "/99")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("resource_not_found"))
                .andExpect(jsonPath("$.message").value("Holding not found with id: 99"));
    }

    @Test
    void delete_sellsEveryUnitAtTheMarketPrice() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1")
                        .param("destinationCbu", "0070009000000000000099")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity()).isNull();
        assertThat(command.getValue().manualQuote()).isNull();
        assertThat(command.getValue().destinationCbu()).isEqualTo(new Cbu("0070009000000000000099"));
    }

    @Test
    void sell_part_returns200WithTheSale() throws Exception {
        when(sellHoldingUseCase.execute(any())).thenReturn(new HoldingSaleResult(
                Money.of(new BigDecimal("800"), "ARS"), Money.of(new BigDecimal("790"), "ARS"),
                new HoldingQuantity(new BigDecimal("4")), new HoldingQuantity(new BigDecimal("6"))));

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4,\"price\":200,\"destinationCbu\":\"0070009000000000000099\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.holdingId").value(1))
                .andExpect(jsonPath("$.data.soldQuantity").value("4"))
                .andExpect(jsonPath("$.data.remainingQuantity").value("6"))
                .andExpect(jsonPath("$.data.proceeds").value("800"))
                .andExpect(jsonPath("$.data.bookedAmount").value("790"))
                .andExpect(jsonPath("$.data.currency").value("ARS"))
                .andExpect(jsonPath("$.data.closed").value(false));

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity().value()).isEqualByComparingTo("4");
        assertThat(command.getValue().manualQuote()).isEqualByComparingTo("200");
    }

    @Test
    void sell_withoutAPrice_asksForTheMarketPrice() throws Exception {
        when(sellHoldingUseCase.execute(any())).thenReturn(new HoldingSaleResult(
                Money.of(new BigDecimal("2000"), "ARS"), Money.of(new BigDecimal("2000"), "ARS"),
                new HoldingQuantity(new BigDecimal("10")), null));

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingQuantity").value("0"))
                .andExpect(jsonPath("$.data.closed").value(true));

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().manualQuote()).isNull();
        assertThat(command.getValue().destinationCbu()).isNull();
    }

    @Test
    void sell_withoutAQuantity_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":200}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));
    }

    @Test
    void sell_aZeroPrice_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1,\"price\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));
    }

    @Test
    void sell_aQuantityWithMoreThanSixDecimals_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"0.0000001\",\"price\":\"10.5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(sellHoldingUseCase, never()).execute(any());
    }

    @Test
    void sell_aQuantityWithThirteenIntegerDigits_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"1234567890123\",\"price\":\"10.5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(sellHoldingUseCase, never()).execute(any());
    }

    @Test
    void sell_aPriceWithMoreThanSixDecimals_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"4\",\"price\":\"10.1234567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(sellHoldingUseCase, never()).execute(any());
    }

    @Test
    void sell_valuesAtTheColumnLimit_areAccepted() throws Exception {
        when(sellHoldingUseCase.execute(any())).thenReturn(new HoldingSaleResult(
                Money.of(new BigDecimal("42"), "ARS"), Money.of(new BigDecimal("42"), "ARS"),
                new HoldingQuantity(new BigDecimal("4")), new HoldingQuantity(new BigDecimal("6"))));

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"999999999999.999999\",\"price\":\"999999999999.999999\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity().value()).isEqualByComparingTo("999999999999.999999");
        assertThat(command.getValue().manualQuote()).isEqualByComparingTo("999999999999.999999");
    }

    @Test
    void sell_trailingZerosCountTowardTheSixDecimals_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"1.2340000\",\"price\":\"10.5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        verify(sellHoldingUseCase, never()).execute(any());
    }

    @Test
    void sell_decimalStrings_deserializeAndReturn200() throws Exception {
        when(sellHoldingUseCase.execute(any())).thenReturn(new HoldingSaleResult(
                Money.of(new BigDecimal("42"), "ARS"), Money.of(new BigDecimal("42"), "ARS"),
                new HoldingQuantity(new BigDecimal("4")), new HoldingQuantity(new BigDecimal("6"))));

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"4\",\"price\":\"10.5\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.soldQuantity").value("4"));

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity().value()).isEqualByComparingTo("4");
        assertThat(command.getValue().manualQuote()).isEqualByComparingTo("10.5");
    }

    @Test
    void sell_decimalNumbers_deserializeAndReturn200() throws Exception {
        when(sellHoldingUseCase.execute(any())).thenReturn(new HoldingSaleResult(
                Money.of(new BigDecimal("42"), "ARS"), Money.of(new BigDecimal("42"), "ARS"),
                new HoldingQuantity(new BigDecimal("1.5")), new HoldingQuantity(new BigDecimal("8.5"))));

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1.5,\"price\":10.25}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SellHoldingCommand> command = ArgumentCaptor.forClass(SellHoldingCommand.class);
        verify(sellHoldingUseCase).execute(command.capture());
        assertThat(command.getValue().quantity().value()).isEqualTo(new BigDecimal("1.5"));
        assertThat(command.getValue().manualQuote()).isEqualTo(new BigDecimal("10.25"));
    }

    @Test
    void sell_moreThanHeld_returns422() throws Exception {
        doThrow(new HoldingSaleExceedsQuantityException(
                new HoldingQuantity(new BigDecimal("11")), new HoldingQuantity(new BigDecimal("10"))))
                .when(sellHoldingUseCase).execute(any());

        mockMvc.perform(post(BASE_URL + "/1/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":11,\"price\":200}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("holding_sale_exceeds_quantity"))
                .andExpect(jsonPath("$.message").value("Cannot sell 11 units of a holding of 10"));
    }

    @Test
    void sell_holdingNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Holding not found: 99")).when(sellHoldingUseCase).execute(any());

        mockMvc.perform(post(BASE_URL + "/99/sell")
                        .header("X-Internal-Token", TOKEN)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("resource_not_found"));
    }

    // --- helpers ---

    private Holding sampleHolding() {
        return new Holding(
                new HoldingId(1L),
                new UserId(USER_ID),
                new BankNumber("007"),
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
        req.setBankNumber("007");
        req.setTicker("AAPL");
        req.setName("Apple Inc");
        req.setAssetType("STOCK");
        req.setQuantity(new BigDecimal("10"));
        req.setAvgPurchasePrice(new BigDecimal("100.00"));
        req.setCurrency("USD");
        return req;
    }
}
