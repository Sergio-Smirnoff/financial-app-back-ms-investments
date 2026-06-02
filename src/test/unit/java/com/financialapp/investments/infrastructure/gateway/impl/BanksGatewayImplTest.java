package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.infrastructure.gateway.BanksClient;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BanksGatewayImplTest {

    @Mock private BanksClient banksClient;
    @InjectMocks private BanksGatewayImpl gateway;

    @Test
    void adjustBalance_delegatesWithCorrectArgs() {
        gateway.adjustBalance(new BanksAccountId(10L), Money.of(new BigDecimal("50"), "ARS"));
        verify(banksClient).adjustBalance(10L, new BigDecimal("50"), "ARS");
    }

    @Test
    void adjustBalance_feignFailure_wrappedAsInfrastructure() {
        Request req = Request.create(Request.HttpMethod.POST, "http://x", Map.of(),
                new byte[0], StandardCharsets.UTF_8, new RequestTemplate());
        FeignException ex = new FeignException.InternalServerError("err", req, null, null);
        doThrow(ex).when(banksClient).adjustBalance(any(), any(), any());

        assertThatThrownBy(() -> gateway.adjustBalance(new BanksAccountId(10L),
                Money.of(BigDecimal.ONE, "ARS")))
                .isInstanceOf(InfrastructureException.class);
    }
}
