package com.financialapp.investments.controller;

import com.financialapp.investments.model.dto.request.HoldingRequest;
import com.financialapp.investments.model.dto.response.ApiResponse;
import com.financialapp.investments.model.dto.response.HoldingResponse;
import com.financialapp.investments.service.HoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/holdings")
@RequiredArgsConstructor
@Tag(name = "Holdings", description = "Investment holdings management")
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping
    @Operation(summary = "List user holdings")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> list(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(holdingService.list(userId)));
    }

    @PostMapping
    @Operation(summary = "Create a new holding")
    public ResponseEntity<ApiResponse<HoldingResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Holding created", holdingService.create(userId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a holding")
    public ResponseEntity<ApiResponse<HoldingResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(holdingService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a holding")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        holdingService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Holding deleted", null));
    }
}
