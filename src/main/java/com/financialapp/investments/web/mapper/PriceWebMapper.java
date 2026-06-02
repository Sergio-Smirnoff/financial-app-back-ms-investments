package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.web.dto.response.PriceHistoryResponse;
import org.springframework.stereotype.Component;

import static com.financialapp.investments.web.mapper.BigDecimals.toPlain;

@Component
public class PriceWebMapper {

    public PriceHistoryResponse toResponse(AssetPriceHistory history) {
        return PriceHistoryResponse.builder()
                .ticker(history.ticker().value())
                .lastPrice(toPlain(history.lastPrice()))
                .openPrice(toPlain(history.openPrice()))
                .highPrice(toPlain(history.highPrice()))
                .lowPrice(toPlain(history.lowPrice()))
                .volume(toPlain(history.volume()))
                .dailyVariation(toPlain(history.dailyVariation()))
                .currency(history.currency())
                .pricedAt(history.pricedAt())
                .build();
    }
}
