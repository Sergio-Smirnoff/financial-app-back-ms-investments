package com.financialapp.investments.infrastructure.gateway;

import com.financialapp.investments.infrastructure.gateway.dto.BanksApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ms-banks", url = "${banks.service.url:http://localhost:8083}")
public interface BanksClient {

    @PostMapping("/api/v1/banks/accounts/{id}/balance/adjust")
    BanksApiResponse<Void> adjustBalance(
            @PathVariable("id") Long id,
            @RequestParam("delta") BigDecimal delta,
            @RequestParam("currency") String currency);
}
