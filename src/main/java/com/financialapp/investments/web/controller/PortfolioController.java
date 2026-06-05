package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.holding.GetHoldingDetailUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioEvolutionCommand;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioEvolutionUseCase;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.web.dto.response.HoldingDetailResponse;
import com.financialapp.investments.web.dto.response.HoldingWithPriceResponse;
import com.financialapp.investments.web.dto.response.PortfolioEvolutionResponse;
import com.financialapp.investments.web.dto.response.PortfolioSummaryResponse;
import com.financialapp.investments.web.mapper.HoldingWebMapper;
import com.financialapp.investments.web.mapper.PortfolioWebMapper;
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

    private final GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;
    private final GetHoldingsWithPricesUseCase getHoldingsWithPricesUseCase;
    private final GetPortfolioEvolutionUseCase getPortfolioEvolutionUseCase;
    private final GetHoldingDetailUseCase getHoldingDetailUseCase;
    private final HoldingWebMapper holdingWebMapper;
    private final PortfolioWebMapper portfolioWebMapper;

    @GetMapping("/summary")
    @Operation(summary = "Get portfolio summary with totals and allocation breakdown")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getSummary(
            @RequestHeader("X-User-Id") Long userId) {
        PortfolioSummaryResponse response = portfolioWebMapper.toResponse(
                getPortfolioSummaryUseCase.execute(new GetPortfolioSummaryCommand(new UserId(userId))));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/holdings")
    @Operation(summary = "List all holdings enriched with current price and P&L")
    public ResponseEntity<ApiResponse<List<HoldingWithPriceResponse>>> getHoldingsWithPrices(
            @RequestHeader("X-User-Id") Long userId) {
        List<HoldingWithPriceResponse> response = getHoldingsWithPricesUseCase
                .execute(new GetHoldingsWithPricesCommand(new UserId(userId)))
                .stream()
                .map(holdingWebMapper::toWithPriceResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/holdings/{holdingId}")
    @Operation(summary = "Get single holding detail with current price and P&L")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found"})
    public ResponseEntity<ApiResponse<HoldingDetailResponse>> getHoldingDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long holdingId) {
        HoldingWithPriceResult result = getHoldingDetailUseCase.execute(
                new GetHoldingDetailCommand(new UserId(userId), new HoldingId(holdingId)));
        return ResponseEntity.ok(ApiResponse.ok(holdingWebMapper.toDetailResponse(result)));
    }

    @GetMapping("/evolution")
    @Operation(summary = "Get historical portfolio evolution (ARS and USD)")
    public ResponseEntity<ApiResponse<List<PortfolioEvolutionResponse>>> getPortfolioEvolution(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "30") int days) {
        List<PortfolioEvolutionResponse> response = getPortfolioEvolutionUseCase
                .execute(new GetPortfolioEvolutionCommand(new UserId(userId), days))
                .stream()
                .map(portfolioWebMapper::toEvolutionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
