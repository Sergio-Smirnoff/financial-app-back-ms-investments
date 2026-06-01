package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.repository.MarketQuoteRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.MarketPanelQuoteJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.MarketQuotePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MarketQuoteRepositoryImpl implements MarketQuoteRepository {

    private final MarketPanelQuoteJpaRepository jpaRepository;
    private final MarketQuotePersistenceMapper mapper;

    @Override
    public void saveAll(List<MarketQuote> quotes) {
        jpaRepository.saveAll(quotes.stream().map(mapper::toEntity).toList());
    }

    @Override
    public List<MarketQuote> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<MarketQuote> findByTicker(Ticker ticker) {
        return jpaRepository.findByTicker(ticker.value()).map(mapper::toDomain);
    }
}
