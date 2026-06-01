package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import com.financialapp.investments.domain.repository.RefreshJobRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.RefreshJobJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.RefreshJobPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshJobRepositoryImpl implements RefreshJobRepository {

    private final RefreshJobJpaRepository jpaRepository;
    private final RefreshJobPersistenceMapper mapper;

    @Override
    public RefreshJob save(RefreshJob job) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(job)));
    }

    @Override
    public Optional<RefreshJob> findLatestInProgress() {
        return jpaRepository.findTopByStatusOrderByStartedAtDesc(RefreshJobStatus.IN_PROGRESS)
                .map(mapper::toDomain);
    }
}
