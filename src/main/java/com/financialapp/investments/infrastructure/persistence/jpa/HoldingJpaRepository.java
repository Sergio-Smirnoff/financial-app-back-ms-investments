package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HoldingJpaRepository extends JpaRepository<HoldingJpaEntity, Long> {

    List<HoldingJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<HoldingJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<HoldingJpaEntity> findByUserIdAndAssetTypeOrderByCreatedAtDesc(
            Long userId, AssetType assetType, Pageable pageable);

    Optional<HoldingJpaEntity> findByIdAndUserId(Long id, Long userId);

    List<HoldingJpaEntity> findByBankAccountId(Long bankAccountId);

    long countByBankAccountId(Long bankAccountId);

    Optional<HoldingJpaEntity> findFirstByTicker(String ticker);

    @Query("SELECT DISTINCT h.userId FROM HoldingJpaEntity h")
    List<Long> findDistinctUserIds();

    @Query("SELECT DISTINCT h.ticker FROM HoldingJpaEntity h")
    List<String> findDistinctTickers();

    @Query("SELECT h FROM HoldingJpaEntity h WHERE h.notifyGainThresholdPct IS NOT NULL OR h.notifyLossThresholdPct IS NOT NULL")
    List<HoldingJpaEntity> findHoldingsWithThresholds();
}
