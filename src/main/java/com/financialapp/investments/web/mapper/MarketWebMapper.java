package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import org.springframework.stereotype.Component;

import static com.financialapp.investments.web.mapper.BigDecimals.toPlain;

@Component
public class MarketWebMapper {

    public MarketDiscoveryResponse toResponse(MarketOpportunityResult result) {
        return MarketDiscoveryResponse.builder()
                .ticker(result.ticker().value())
                .price(toPlain(result.price().amount()))
                .currency(result.price().currency().getCurrencyCode())
                .variation(toPlain(result.variation()))
                .volume(toPlain(result.volume()))
                .build();
    }
}
