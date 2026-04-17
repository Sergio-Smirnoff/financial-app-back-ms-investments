package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.AssetPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssetPriceHistoryRepository extends JpaRepository<AssetPriceHistory, Long> {

    List<AssetPriceHistory> findByTickerAndPricedAtBetweenOrderByPricedAtAsc(
            String ticker, LocalDateTime from, LocalDateTime to);

    List<AssetPriceHistory> findByTickerOrderByPricedAtAsc(String ticker);
}
