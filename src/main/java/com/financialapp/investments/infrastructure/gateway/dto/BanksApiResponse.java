package com.financialapp.investments.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BanksApiResponse<T>(boolean success, String message, T data) {}
