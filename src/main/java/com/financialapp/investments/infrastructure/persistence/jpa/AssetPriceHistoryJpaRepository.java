package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AssetPriceHistoryJpaRepository extends JpaRepository<AssetPriceHistoryJpaEntity, Long> {

    List<AssetPriceHistoryJpaEntity> findByTickerOrderByPricedAtAsc(String ticker);

    List<AssetPriceHistoryJpaEntity> findByTickerAndPricedAtBetweenOrderByPricedAtAsc(
            String ticker, LocalDateTime from, LocalDateTime to);

    long countByTickerAndPricedAtBetween(String ticker, LocalDateTime from, LocalDateTime to);

    boolean existsByTickerAndPricedAt(String ticker, LocalDateTime pricedAt);
}
