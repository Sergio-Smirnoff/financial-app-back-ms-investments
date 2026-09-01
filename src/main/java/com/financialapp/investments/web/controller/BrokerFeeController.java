package com.financialapp.investments.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.usecase.fee.ListBrokerFeeSchedules;
import com.financialapp.investments.domain.usecase.fee.UpsertBrokerFeeSchedule;
import com.financialapp.investments.web.dto.request.BrokerFeeScheduleRequest;
import com.financialapp.investments.web.dto.response.BrokerFeeScheduleResponse;
import com.financialapp.investments.web.mapper.BrokerFeeWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments/fees/brokers")
@RequiredArgsConstructor
@Tag(name = "Broker Fees", description = "Management of broker fee schedules and netting rules")
public class BrokerFeeController {

    private final UpsertBrokerFeeSchedule upsertBrokerFeeSchedule;
    private final ListBrokerFeeSchedules listBrokerFeeSchedules;

    @PutMapping("/{bankNumber}")
    @Operation(summary = "Upsert fee schedule for a broker/bank")
    public ResponseEntity<ApiResponse<BrokerFeeScheduleResponse>> upsert(
            @PathVariable String bankNumber,
            @RequestBody BrokerFeeScheduleRequest request) {

        BrokerFeeSchedule saved = upsertBrokerFeeSchedule.execute(BrokerFeeWebMapper.toCommand(bankNumber, request));
        return ResponseEntity.ok(ApiResponse.ok(BrokerFeeWebMapper.toResponse(saved)));
    }

    @GetMapping
    @Operation(summary = "List all broker fee schedules")
    public ResponseEntity<ApiResponse<List<BrokerFeeScheduleResponse>>> listAll() {
        List<BrokerFeeSchedule> schedules = listBrokerFeeSchedules.execute();
        List<BrokerFeeScheduleResponse> response = schedules.stream()
                .map(BrokerFeeWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
