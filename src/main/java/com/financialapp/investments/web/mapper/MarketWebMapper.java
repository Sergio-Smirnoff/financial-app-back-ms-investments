package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;
import com.financialapp.investments.domain.usecase.market.response.TickerSearchResult;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import com.financialapp.investments.web.dto.response.TickerResearchResponse;
import com.financialapp.investments.web.dto.response.TickerSearchResponse;
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

    public TickerSearchResponse toSearchResponse(TickerSearchResult result) {
        return TickerSearchResponse.builder()
                .ticker(result.ticker().value())
                .price(toPlain(result.price().amount()))
                .currency(result.price().currency().getCurrencyCode())
                .variation(toPlain(result.variation()))
                .build();
    }

    public TickerResearchResponse toResearchResponse(TickerResearchResult research) {
        return TickerResearchResponse.builder()
                .ticker(research.ticker().value())
                .currency(research.currentQuote().map(PriceDetail::currency).orElse(null))
                .currentPrice(research.currentQuote().map(quote -> toPlain(quote.lastPrice())).orElse(null))
                .variation(research.currentQuote().map(quote -> toPlain(quote.dailyVariation())).orElse(null))
                .series(research.series().stream()
                        .map(point -> TickerResearchResponse.Point.builder()
                                .date(point.pricedAt().toLocalDate().toString())
                                .price(toPlain(point.lastPrice()))
                                .build())
                        .toList())
                .build();
    }
}
