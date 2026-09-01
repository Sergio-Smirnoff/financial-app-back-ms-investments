package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.usecase.holding.command.*;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import com.financialapp.investments.domain.usecase.holding.*;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.web.dto.request.HoldingRequest;
import com.financialapp.investments.web.dto.response.AccountValuationResponse;
import com.financialapp.investments.web.dto.response.HoldingResponse;
import com.financialapp.investments.web.mapper.HoldingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/holdings")
@RequiredArgsConstructor
@Tag(name = "Holdings", description = "Investment holdings management")
public class HoldingController {

    private final CreateHoldingUseCase createHoldingUseCase;
    private final UpdateHoldingUseCase updateHoldingUseCase;
    private final CloseHoldingUseCase closeHoldingUseCase;
    private final ListHoldingsUseCase listHoldingsUseCase;
    private final GetAccountValuationUseCase getAccountValuationUseCase;
    private final HoldingWebMapper holdingWebMapper;

    @GetMapping
    @Operation(summary = "List user holdings (paginated)")
    public ResponseEntity<ApiResponse<Page<HoldingResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String assetType,
            @PageableDefault(size = 20) Pageable pageable) {
        AssetType type = assetType != null ? AssetType.valueOf(assetType.toUpperCase()) : null;
        PageResult<Holding> result = listHoldingsUseCase.execute(new ListHoldingsCommand(
                new UserId(userId), type,
                new PageRequest(pageable.getPageNumber(), pageable.getPageSize())));
        List<HoldingResponse> content = result.content().stream()
                .map(holdingWebMapper::toResponse).toList();
        Page<HoldingResponse> page = new PageImpl<>(content, pageable, result.totalElements());
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/valuation")
    @Operation(summary = "Get total valuation for a user's holdings in a bank+currency")
    public ResponseEntity<ApiResponse<AccountValuationResponse>> getAccountValuation(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String bankNumber,
            @RequestParam String currency) {
        AccountValuationResult result = getAccountValuationUseCase.execute(
                new GetAccountValuationCommand(new UserId(userId), new BankNumber(bankNumber),
                        Currency.getInstance(currency)));
        return ResponseEntity.ok(ApiResponse.ok(holdingWebMapper.toValuationResponse(result)));
    }

    @PostMapping
    @Operation(summary = "Create a new holding")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_already_exists", "holding_quantity_invalid", "holding_currency_mismatch", "unsupported_currency", "banks_service_unavailable"})
    public ResponseEntity<ApiResponse<HoldingResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody HoldingRequest request) {
        Holding holding = createHoldingUseCase.execute(toCreateCommand(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Holding created", holdingWebMapper.toResponse(holding)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a holding")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "holding_quantity_invalid", "holding_currency_mismatch", "unsupported_currency"})
    public ResponseEntity<ApiResponse<HoldingResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody HoldingRequest request) {
        Holding holding = updateHoldingUseCase.execute(toUpdateCommand(id, userId, request));
        return ResponseEntity.ok(ApiResponse.ok(holdingWebMapper.toResponse(holding)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (Sell) a holding")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "resource_conflict", "finances_service_unavailable"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestParam(required = false) String destinationCbu) {
        closeHoldingUseCase.execute(new CloseHoldingCommand(
                new UserId(userId),
                new HoldingId(id),
                destinationCbu != null ? new Cbu(destinationCbu) : null));
        return ResponseEntity.ok(ApiResponse.ok("Holding deleted", null));
    }

    private CreateHoldingCommand toCreateCommand(Long userId, HoldingRequest req) {
        return new CreateHoldingCommand(
                new UserId(userId),
                new BankNumber(req.getBankNumber()),
                new Ticker(req.getTicker()),
                req.getName(),
                AssetType.valueOf(req.getAssetType()),
                new HoldingQuantity(req.getQuantity()),
                Money.of(req.getAvgPurchasePrice(), req.getCurrency()),
                new ThresholdConfig(req.getNotifyGainThresholdPct(), req.getNotifyLossThresholdPct()),
                req.getFundingCbu() != null ? new Cbu(req.getFundingCbu()) : null
        );
    }

    private UpdateHoldingCommand toUpdateCommand(Long id, Long userId, HoldingRequest req) {
        return new UpdateHoldingCommand(
                new UserId(userId),
                new HoldingId(id),
                new BankNumber(req.getBankNumber()),
                new Ticker(req.getTicker()),
                req.getName(),
                AssetType.valueOf(req.getAssetType()),
                new HoldingQuantity(req.getQuantity()),
                Money.of(req.getAvgPurchasePrice(), req.getCurrency()),
                new ThresholdConfig(req.getNotifyGainThresholdPct(), req.getNotifyLossThresholdPct()),
                req.getFundingCbu() != null ? new Cbu(req.getFundingCbu()) : null
        );
    }
}
