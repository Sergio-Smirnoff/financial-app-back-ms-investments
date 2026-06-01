package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetPriceJpaRepository extends JpaRepository<AssetPriceJpaEntity, Long> {

    Optional<AssetPriceJpaEntity> findByTicker(String ticker);

    List<AssetPriceJpaEntity> findAllByTickerIn(Collection<String> tickers);
}
