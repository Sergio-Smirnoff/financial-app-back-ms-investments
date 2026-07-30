package com.financialapp.investments.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.BrokerFeeScheduleId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.fee.ListBrokerFeeSchedules;
import com.financialapp.investments.domain.usecase.fee.UpsertBrokerFeeSchedule;
import com.financialapp.investments.web.dto.request.BrokerFeeScheduleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrokerFeeController.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class BrokerFeeControllerTest {

    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpsertBrokerFeeSchedule upsertBrokerFeeSchedule;

    @MockBean
    private ListBrokerFeeSchedules listBrokerFeeSchedules;

    @Test
    void upsert_validRequest_returns200InEnvelope() throws Exception {
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                new BrokerFeeScheduleId(1L),
                new BankNumber("007"),
                AssetType.STOCK,
                new BigDecimal("0.50"),
                new BigDecimal("0.50"),
                Money.of(new BigDecimal("100.00"), "ARS"),
                new BigDecimal("0.10"),
                IvaTreatment.SEPARATE
        );
        when(upsertBrokerFeeSchedule.execute(any())).thenReturn(schedule);

        BrokerFeeScheduleRequest request = new BrokerFeeScheduleRequest();
        request.setAssetType("STOCK");
        request.setBuyFeePct(new BigDecimal("0.50"));
        request.setSellFeePct(new BigDecimal("0.50"));
        request.setMinimumFee(new BigDecimal("100.00"));
        request.setMarketFeePct(new BigDecimal("0.10"));
        request.setIvaTreatment("SEPARATE");
        request.setCurrency("ARS");

        mockMvc.perform(put("/api/v1/investments/fees/brokers/007")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bankNumber").value("007"))
                .andExpect(jsonPath("$.data.assetType").value("STOCK"))
                .andExpect(jsonPath("$.data.buyFeePct").value(0.50));
    }

    @Test
    void list_returnsSchedulesInEnvelope() throws Exception {
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                new BrokerFeeScheduleId(1L),
                new BankNumber("007"),
                null,
                new BigDecimal("0.50"),
                new BigDecimal("0.50"),
                null,
                new BigDecimal("0.10"),
                IvaTreatment.SEPARATE
        );
        when(listBrokerFeeSchedules.execute()).thenReturn(List.of(schedule));

        mockMvc.perform(get("/api/v1/investments/fees/brokers")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].bankNumber").value("007"));
    }
}
