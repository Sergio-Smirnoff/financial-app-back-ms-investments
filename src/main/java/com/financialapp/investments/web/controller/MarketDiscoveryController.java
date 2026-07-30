package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.PriceRange;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.market.GetMarketDiscoveryUseCase;
import com.financialapp.investments.domain.usecase.market.GetTickerResearchUseCase;
import com.financialapp.investments.domain.usecase.market.SearchTickersUseCase;
import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.command.GetTickerResearchCommand;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import com.financialapp.investments.web.dto.response.TickerResearchResponse;
import com.financialapp.investments.web.dto.response.TickerSearchResponse;
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
    private final SearchTickersUseCase searchTickersUseCase;
    private final GetTickerResearchUseCase getTickerResearchUseCase;
    private final com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase getMarketPanelUseCase;
    private final MarketWebMapper marketWebMapper;

    @GetMapping("/panel")
    @Operation(summary = "Get widened market panel containing quotes, indices, and latest FX rates")
    public ResponseEntity<ApiResponse<com.financialapp.investments.web.dto.response.MarketPanelResponse>> getPanel() {
        com.financialapp.investments.web.dto.response.MarketPanelResponse response = marketWebMapper.toPanelResponse(getMarketPanelUseCase.execute());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/discovery")
    @Operation(summary = "Get trending assets not in user portfolio")
    public ResponseEntity<ApiResponse<MarketDiscoveryResponse>> getDiscovery(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        MarketDiscoveryResponse response = marketWebMapper.toResponse(
                getMarketDiscoveryUseCase.execute(new GetMarketDiscoveryCommand(new UserId(userId), limit)));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search the cached market panel by ticker symbol")
    public ResponseEntity<ApiResponse<List<TickerSearchResponse>>> search(@RequestParam String q) {
        List<TickerSearchResponse> response = searchTickersUseCase.execute(q).stream()
                .map(marketWebMapper::toSearchResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/tickers/{ticker}")
    @Operation(summary = "Live research for a ticker: current quote + historical series")
    public ResponseEntity<ApiResponse<TickerResearchResponse>> research(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "STOCK") String assetType,
            @RequestParam(defaultValue = "D90") String range) {
        TickerResearchResult result = getTickerResearchUseCase.execute(new GetTickerResearchCommand(
                new Ticker(ticker), AssetType.valueOf(assetType.toUpperCase()), PriceRange.of(range)));
        return ResponseEntity.ok(ApiResponse.ok(marketWebMapper.toResearchResponse(result)));
    }
}
