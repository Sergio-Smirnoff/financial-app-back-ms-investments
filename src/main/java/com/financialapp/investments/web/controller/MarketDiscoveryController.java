package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.GetMarketDiscoveryUseCase;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import com.financialapp.investments.web.mapper.MarketWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/market")
@RequiredArgsConstructor
@Tag(name = "Market Discovery", description = "Trending investment opportunities")
public class MarketDiscoveryController {

    private final GetMarketDiscoveryUseCase getMarketDiscoveryUseCase;
    private final MarketWebMapper marketWebMapper;

    @GetMapping("/discovery")
    @Operation(summary = "Get trending assets not in user portfolio")
    public ResponseEntity<ApiResponse<List<MarketDiscoveryResponse>>> getDiscovery(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<MarketDiscoveryResponse> response = getMarketDiscoveryUseCase
                .execute(new GetMarketDiscoveryCommand(new UserId(userId), limit))
                .stream()
                .map(marketWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
