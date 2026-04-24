package com.financialapp.investments.controller;

import com.financialapp.investments.model.dto.response.ApiResponse;
import com.financialapp.investments.model.dto.response.HoldingWithPriceResponse;
import com.financialapp.investments.model.dto.response.PortfolioEvolutionResponse;
import com.financialapp.investments.model.dto.response.PortfolioSummaryResponse;
import com.financialapp.investments.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio summary and enriched holdings")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/summary")
    @Operation(summary = "Get portfolio summary with totals and allocation breakdown")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getSummary(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.getSummary(userId)));
    }

    @GetMapping("/holdings")
    @Operation(summary = "List all holdings enriched with current price and P&L")
    public ResponseEntity<ApiResponse<List<HoldingWithPriceResponse>>> getHoldingsWithPrices(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.getHoldingsWithPrices(userId)));
    }

    @GetMapping("/evolution")
    @Operation(summary = "Get historical portfolio evolution (ARS and USD)")
    public ResponseEntity<ApiResponse<List<PortfolioEvolutionResponse>>> getPortfolioEvolution(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.getPortfolioEvolution(userId, days)));
    }
}
