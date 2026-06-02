package com.financialapp.investments.infrastructure.gateway.dto;

import java.time.LocalDate;

public record RecordTransactionRequest(
        String fromCbu,
        String toCbu,
        String amount,
        String currency,
        Long categoryId,
        String description,
        LocalDate date) {}
