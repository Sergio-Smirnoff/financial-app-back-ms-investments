package com.financialapp.investments.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PortfolioEvolutionResponse {
    private LocalDate date;
    private BigDecimal totalValueArs;
    private BigDecimal totalValueUsd;
}
