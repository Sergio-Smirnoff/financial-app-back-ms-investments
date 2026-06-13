package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TickerResearchResponse {
    private String ticker;
    private String currency;
    private String currentPrice;
    private String variation;
    private List<Point> series;

    @Getter
    @Builder
    public static class Point {
        private String date;
        private String price;
    }
}
