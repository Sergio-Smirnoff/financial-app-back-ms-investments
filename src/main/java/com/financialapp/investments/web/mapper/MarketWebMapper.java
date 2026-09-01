package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.usecase.market.response.MarketDiscoveryResult;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;
import com.financialapp.investments.domain.usecase.market.response.TickerSearchResult;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import com.financialapp.investments.web.dto.response.TickerResearchResponse;
import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase;
import com.financialapp.investments.web.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.financialapp.investments.web.mapper.BigDecimals.toPlain;

@Component
public class MarketWebMapper {

    public MarketDiscoveryResponse toResponse(MarketDiscoveryResult result) {
        return MarketDiscoveryResponse.builder()
                .marketDataAvailable(result.marketDataAvailable())
                .opportunities(result.opportunities().stream()
                        .map(o -> MarketDiscoveryResponse.Opportunity.builder()
                                .ticker(o.ticker().value())
                                .price(toPlain(o.price().amount()))
                                .currency(o.price().currency().getCurrencyCode())
                                .variation(toPlain(o.variation()))
                                .volume(toPlain(o.volume()))
                                .build())
                        .toList())
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
                .series(research.series().points().stream()
                        .map(point -> TickerResearchResponse.Point.builder()
                                .date(point.pricedAt().toLocalDate().toString())
                                .price(toPlain(point.lastPrice()))
                                .build())
                        .toList())
                .build();
    }

    public MarketIndexResponse toIndexResponse(com.financialapp.investments.domain.model.market.MarketIndex index) {
        if (index == null) return null;
        return new MarketIndexResponse(
                index.code(),
                index.value() != null ? index.value().toPlainString() : null,
                index.variation() != null ? index.variation().toPlainString() : null
        );
    }

    public com.financialapp.investments.web.dto.response.MarketQuotePanelResponse toQuotePanelResponse(com.financialapp.investments.domain.model.market.MarketQuote quote) {
        if (quote == null) return null;
        return new com.financialapp.investments.web.dto.response.MarketQuotePanelResponse(
                quote.ticker().value(),
                quote.price() != null ? quote.price().amount().toPlainString() : null,
                quote.variation() != null ? quote.variation().toPlainString() : null
        );
    }

    public com.financialapp.investments.web.dto.response.MarketPanelResponse toPanelResponse(com.financialapp.investments.domain.usecase.market.GetMarketPanelUseCase.MarketPanelResult result) {
        if (result == null) return null;
        var quotes = result.quotes().stream().map(this::toQuotePanelResponse).toList();
        var indices = result.indices().stream().map(this::toIndexResponse).toList();
        var fxRates = result.fxRates().stream().map(FxRateWebMapper::toResponse).toList();

        return new com.financialapp.investments.web.dto.response.MarketPanelResponse(quotes, indices, fxRates);
    }
}
