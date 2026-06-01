package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.infrastructure.persistence.entity.MarketPanelQuoteJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MarketQuotePersistenceMapper {

    public MarketQuote toDomain(MarketPanelQuoteJpaEntity e) {
        String currency = e.getCurrency() != null ? e.getCurrency() : "ARS";
        return new MarketQuote(
                new Ticker(e.getTicker()),
                Money.of(e.getLastPrice(), currency),
                e.getVariation(),
                e.getVolume(),
                e.getUpdatedAt()
        );
    }

    public MarketPanelQuoteJpaEntity toEntity(MarketQuote q) {
        return MarketPanelQuoteJpaEntity.builder()
                .ticker(q.ticker().value())
                .lastPrice(q.price().amount())
                .currency(q.price().currency().getCurrencyCode())
                .variation(q.variation())
                .volume(q.volume())
                .updatedAt(q.updatedAt() != null ? q.updatedAt() : LocalDateTime.now())
                .build();
    }
}
