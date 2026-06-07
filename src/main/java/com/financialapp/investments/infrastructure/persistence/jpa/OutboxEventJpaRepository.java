package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findBySentFalseOrderByIdAsc(Limit limit);

    Optional<OutboxEventEntity> findByEventId(String eventId);
}
