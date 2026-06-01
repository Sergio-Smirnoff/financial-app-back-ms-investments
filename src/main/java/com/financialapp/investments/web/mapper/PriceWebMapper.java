package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.web.dto.response.PriceHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class PriceWebMapper {

    public PriceHistoryResponse toResponse(AssetPriceHistory history) {
        return PriceHistoryResponse.builder()
                .ticker(history.ticker().value())
                .lastPrice(history.lastPrice())
                .openPrice(history.openPrice())
                .highPrice(history.highPrice())
                .lowPrice(history.lowPrice())
                .volume(history.volume())
                .dailyVariation(history.dailyVariation())
                .currency(history.currency())
                .pricedAt(history.pricedAt())
                .build();
    }
}
