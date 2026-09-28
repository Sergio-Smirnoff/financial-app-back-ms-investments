package com.financialapp.investments.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.web.dto.response.EnsuredSnapshotResponse;
import com.financialapp.investments.web.mapper.PortfolioWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investments/portfolio/snapshot")
@RequiredArgsConstructor
@Tag(name = "Portfolio snapshot", description = "Idempotent daily portfolio snapshot")
public class PortfolioSnapshotController {

    private final EnsurePortfolioSnapshotUseCase ensurePortfolioSnapshotUseCase;
    private final PortfolioWebMapper portfolioWebMapper;

    @PostMapping("/ensure-today")
    @Operation(summary = "Capture today's portfolio snapshot for the caller unless it already exists")
    public ResponseEntity<ApiResponse<EnsuredSnapshotResponse>> ensureToday(
            @RequestHeader("X-User-Id") Long userId) {
        EnsuredSnapshotResponse response = portfolioWebMapper.toEnsuredSnapshotResponse(
                ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(new UserId(userId))));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
