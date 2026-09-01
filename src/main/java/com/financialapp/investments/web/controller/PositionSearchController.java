package com.financialapp.investments.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.usecase.portfolio.SearchPositions;
import com.financialapp.investments.web.dto.response.PositionSearchResponse;
import com.financialapp.investments.web.mapper.PortfolioWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/positions")
@RequiredArgsConstructor
@Tag(name = "Positions", description = "User positions search and management")
public class PositionSearchController {

    private final SearchPositions searchPositions;
    private final PortfolioWebMapper portfolioWebMapper;

    @GetMapping("/search")
    @Operation(summary = "Search user positions by ticker or name")
    public ResponseEntity<ApiResponse<List<PositionSearchResponse>>> search(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String q) {
        List<Holding> holdings = searchPositions.execute(new UserId(userId), q);
        List<PositionSearchResponse> responses = holdings.stream()
                .map(portfolioWebMapper::toPositionSearchResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
