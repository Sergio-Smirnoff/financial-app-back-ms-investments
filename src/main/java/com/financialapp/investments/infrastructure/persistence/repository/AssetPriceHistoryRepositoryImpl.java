package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.repository.AssetPriceHistoryRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.AssetPriceHistoryJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.AssetPriceHistoryPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetPriceHistoryRepositoryImpl implements AssetPriceHistoryRepository {

    private final AssetPriceHistoryJpaRepository jpaRepository;
    private final AssetPriceHistoryPersistenceMapper mapper;

    @Override
    public AssetPriceHistory save(AssetPriceHistory history) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(history)));
    }

    @Override
    public List<AssetPriceHistory> findByTicker(Ticker ticker) {
        return jpaRepository.findByTickerOrderByPricedAtAsc(ticker.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AssetPriceHistory> findByTickerAndPricedAtBetween(
            Ticker ticker, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByTickerAndPricedAtBetweenOrderByPricedAtAsc(
                ticker.value(), from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByTickerAndPricedAtBetween(Ticker ticker, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.countByTickerAndPricedAtBetween(ticker.value(), from, to);
    }

    @Override
    public boolean existsByTickerAndPricedAt(Ticker ticker, LocalDateTime pricedAt) {
        return jpaRepository.existsByTickerAndPricedAt(ticker.value(), pricedAt);
    }
}
