package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TickerSearchResponse {
    private String ticker;
    private String price;
    private String currency;
    private String variation;
}
