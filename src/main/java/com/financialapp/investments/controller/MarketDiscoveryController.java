package com.financialapp.investments.controller;

import com.financialapp.investments.model.dto.internal.MarketQuote;
import com.financialapp.investments.model.dto.response.ApiResponse;
import com.financialapp.investments.service.MarketDiscoveryService;
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

    private final MarketDiscoveryService marketDiscoveryService;

    @GetMapping("/discovery")
    @Operation(summary = "Get trending assets not in user portfolio")
    public ResponseEntity<ApiResponse<List<MarketQuote>>> getDiscovery(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(marketDiscoveryService.getTrendingOpportunities(userId, limit)));
    }
}
