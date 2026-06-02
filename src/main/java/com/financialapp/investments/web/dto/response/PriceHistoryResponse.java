package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PriceHistoryResponse {
    private String ticker;
    private String lastPrice;
    private String openPrice;
    private String highPrice;
    private String lowPrice;
    private String volume;
    private String dailyVariation;
    private String currency;
    private LocalDateTime pricedAt;
}
