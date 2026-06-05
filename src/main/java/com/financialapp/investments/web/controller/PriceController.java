package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.usecase.price.RefreshPricesUseCase;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.investments.domain.exception.DomainError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investments/prices")
@RequiredArgsConstructor
@Tag(name = "Prices", description = "Asset price management")
public class PriceController {

    private final RefreshPricesUseCase refreshPricesUseCase;

    @PostMapping("/refresh")
    @Operation(summary = "Manually trigger price refresh for all held tickers")
    @ApiErrorCodes(catalog = DomainError.class, value = {"iol_service_unavailable"})
    public ResponseEntity<ApiResponse<Void>> refresh() {
        refreshPricesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok("Price refresh triggered", null));
    }
}
