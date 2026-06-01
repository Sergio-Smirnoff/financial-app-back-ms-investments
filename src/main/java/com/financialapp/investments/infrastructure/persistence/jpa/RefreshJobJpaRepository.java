package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import com.financialapp.investments.infrastructure.persistence.entity.RefreshJobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshJobJpaRepository extends JpaRepository<RefreshJobJpaEntity, Long> {

    Optional<RefreshJobJpaEntity> findTopByStatusOrderByStartedAtDesc(RefreshJobStatus status);
}
