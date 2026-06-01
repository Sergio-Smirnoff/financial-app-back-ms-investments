package com.financialapp.investments.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "iol")
public record IolProperties(
        @NotBlank String baseUrl,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Pattern(
                regexp = "^\\S+(\\s+\\S+){5}$",
                message = "priceRefreshCron must be a 6-field cron expression"
        ) String priceRefreshCron
) {}
