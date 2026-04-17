package com.financialapp.investments.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    private String ticker;
    private BigDecimal lastPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;
    private BigDecimal dailyVariation;
    private String currency;
    private LocalDateTime pricedAt;
}
