package com.financialapp.investments.infrastructure.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "investments.currencies")
public record CurrenciesProperties(
        @NotEmpty Set<@Pattern(regexp = "[A-Z]{3}") String> supported
) {}
