package com.financialapp.investments.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxSnapshotRates;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates.BackfillFxRatesCommand;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates.BackfillResult;
import com.financialapp.investments.domain.usecase.fx.GetFxRates;
import com.financialapp.investments.domain.usecase.fx.GetFxRates.GetFxRatesCommand;
import com.financialapp.investments.domain.usecase.fx.GetFxRatesAtDate;
import com.financialapp.investments.domain.usecase.fx.GetFxRatesAtDate.GetFxRatesAtDateCommand;
import com.financialapp.investments.domain.usecase.fx.GetLatestFxRates;
import com.financialapp.investments.web.dto.response.BackfillResultResponse;
import com.financialapp.investments.web.dto.response.FxRateResponse;
import com.financialapp.investments.web.dto.response.FxSnapshotRatesResponse;
import com.financialapp.investments.web.mapper.FxRateWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/fx/rates")
@RequiredArgsConstructor
@Tag(name = "FX Rates", description = "Persisted and computed exchange rate queries")
public class FxRateController {

    private final GetFxRates getFxRates;
    private final GetLatestFxRates getLatestFxRates;
    private final GetFxRatesAtDate getFxRatesAtDate;
    private final BackfillFxRates backfillFxRates;

    @GetMapping
    @Operation(summary = "Get historical persisted FX rates")
    public ResponseEntity<ApiResponse<List<FxRateResponse>>> getRates(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String view) {

        FxView fxView = view != null && !view.isBlank() ? FxView.valueOf(view.toUpperCase()) : null;
        List<FxRate> rates = getFxRates.execute(new GetFxRatesCommand(from, to, fxView));
        List<FxRateResponse> response = rates.stream().map(FxRateWebMapper::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest persisted FX rate for each view")
    public ResponseEntity<ApiResponse<List<FxRateResponse>>> getLatestRates() {
        List<FxRate> rates = getLatestFxRates.execute();
        List<FxRateResponse> response = rates.stream().map(FxRateWebMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/at")
    @Operation(summary = "Computed FX rates passthrough at a specific date (never persisted)")
    public ResponseEntity<ApiResponse<FxSnapshotRatesResponse>> getRatesAtDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        FxSnapshotRates snapshot = getFxRatesAtDate.execute(new GetFxRatesAtDateCommand(date));
        return ResponseEntity.ok(ApiResponse.ok(FxRateWebMapper.toResponse(snapshot)));
    }

    @PostMapping("/backfill")
    @Operation(summary = "Idempotent backfill of FX rates for a date range")
    public ResponseEntity<ApiResponse<BackfillResultResponse>> backfill(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        BackfillResult result = backfillFxRates.execute(new BackfillFxRatesCommand(from, to));
        return ResponseEntity.ok(ApiResponse.ok(new BackfillResultResponse(result.createdCount())));
    }
}
