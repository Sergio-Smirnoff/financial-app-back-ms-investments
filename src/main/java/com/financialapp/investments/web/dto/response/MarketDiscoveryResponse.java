package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MarketDiscoveryResponse {
    private boolean marketDataAvailable;
    private List<Opportunity> opportunities;

    @Getter
    @Builder
    public static class Opportunity {
        private String ticker;
        private String price;
        private String currency;
        private String variation;
        private String volume;
    }
}
