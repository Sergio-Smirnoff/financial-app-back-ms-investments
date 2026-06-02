package com.financialapp.investments.infrastructure.gateway;

import com.financialapp.investments.infrastructure.gateway.dto.FinancesApiResponse;
import com.financialapp.investments.infrastructure.gateway.dto.RecordTransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-finances", url = "${finances.service.url:http://localhost:8082}")
public interface FinancesClient {

    @PostMapping("/api/v1/finances/transactions")
    FinancesApiResponse<Object> recordTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RecordTransactionRequest request);
}
