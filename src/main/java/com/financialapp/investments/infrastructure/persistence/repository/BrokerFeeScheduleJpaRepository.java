package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.infrastructure.persistence.entity.BrokerFeeScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BrokerFeeScheduleJpaRepository extends JpaRepository<BrokerFeeScheduleJpaEntity, Long> {

    @Query("SELECT b FROM BrokerFeeScheduleJpaEntity b WHERE b.bankNumber = :bankNumber AND " +
           "((:assetType IS NOT NULL AND b.assetType = :assetType) OR b.assetType IS NULL) " +
           "ORDER BY CASE WHEN b.assetType IS NOT NULL THEN 0 ELSE 1 END LIMIT 1")
    Optional<BrokerFeeScheduleJpaEntity> findForBankAndAssetType(
            @Param("bankNumber") String bankNumber,
            @Param("assetType") String assetType
    );

    Optional<BrokerFeeScheduleJpaEntity> findByBankNumberAndAssetType(String bankNumber, String assetType);
}
