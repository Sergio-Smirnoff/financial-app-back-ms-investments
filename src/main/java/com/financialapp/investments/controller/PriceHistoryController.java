package com.financialapp.investments.controller;

import com.financialapp.investments.model.dto.response.ApiResponse;
import com.financialapp.investments.model.dto.response.PriceHistoryResponse;
import com.financialapp.investments.model.entity.AssetPriceHistory;
import com.financialapp.investments.service.PriceHistoryService;
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

    private final PriceHistoryService priceHistoryService;

    @GetMapping("/{ticker}")
    public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getHistory(
            @PathVariable String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<PriceHistoryResponse> history = priceHistoryService.getHistory(ticker, from, to)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok("Price history retrieved", history));
    }

    private PriceHistoryResponse toResponse(AssetPriceHistory h) {
        return PriceHistoryResponse.builder()
                .ticker(h.getTicker())
                .lastPrice(h.getLastPrice())
                .openPrice(h.getOpenPrice())
                .highPrice(h.getHighPrice())
                .lowPrice(h.getLowPrice())
                .volume(h.getVolume())
                .dailyVariation(h.getDailyVariation())
                .currency(h.getCurrency())
                .pricedAt(h.getPricedAt())
                .build();
    }
}
