package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.AssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetPriceRepository extends JpaRepository<AssetPrice, Long> {

    Optional<AssetPrice> findByTicker(String ticker);

    List<AssetPrice> findAllByTickerIn(Collection<String> tickers);
}
