package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketDiscoveryResponse {
    private String ticker;
    private String price;
    private String currency;
    private String variation;
    private String volume;
}
