package com.financialapp.investments.infrastructure.gateway.dto;

public record FinancesApiResponse<T>(boolean success, String message, T data) {}
