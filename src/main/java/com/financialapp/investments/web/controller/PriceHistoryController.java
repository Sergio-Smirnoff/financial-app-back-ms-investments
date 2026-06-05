package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import com.financialapp.investments.domain.usecase.price.GetPriceHistoryUseCase;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.web.dto.response.PriceHistoryResponse;
import com.financialapp.investments.web.mapper.PriceWebMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/prices/history")
@RequiredArgsConstructor
@Tag(name = "Price History")
public class PriceHistoryController {

    // Missing range = "all history": fall back to a wide look-back window.
    private static final int DEFAULT_LOOKBACK_YEARS = 10;

    private final GetPriceHistoryUseCase getPriceHistoryUseCase;
    private final PriceWebMapper priceWebMapper;

    @GetMapping("/{ticker}")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found"})
    public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getHistory(
            @PathVariable String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        LocalDateTime resolvedTo = to != null ? to : LocalDateTime.now();
        LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusYears(DEFAULT_LOOKBACK_YEARS);
        List<PriceHistoryResponse> history = getPriceHistoryUseCase
                .execute(new GetPriceHistoryCommand(new Ticker(ticker), resolvedFrom, resolvedTo))
                .stream()
                .map(priceWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Price history retrieved", history));
    }
}
