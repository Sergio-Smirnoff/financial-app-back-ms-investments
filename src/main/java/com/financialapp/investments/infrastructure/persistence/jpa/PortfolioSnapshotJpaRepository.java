package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.infrastructure.persistence.entity.PortfolioSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotJpaRepository extends JpaRepository<PortfolioSnapshotJpaEntity, Long> {

    List<PortfolioSnapshotJpaEntity> findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
            Long userId, LocalDate date);
}
