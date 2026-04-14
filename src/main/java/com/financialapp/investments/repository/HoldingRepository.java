package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.Holding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Holding> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Holding> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT h.ticker FROM Holding h")
    List<String> findDistinctTickers();

    Optional<Holding> findFirstByTicker(String ticker);

    @Query("SELECT h FROM Holding h WHERE h.notifyGainThresholdPct IS NOT NULL OR h.notifyLossThresholdPct IS NOT NULL")
    List<Holding> findHoldingsWithThresholds();
}
