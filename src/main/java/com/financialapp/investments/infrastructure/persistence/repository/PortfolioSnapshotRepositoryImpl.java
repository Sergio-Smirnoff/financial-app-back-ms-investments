package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.PortfolioSnapshotJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.PortfolioSnapshotPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PortfolioSnapshotRepositoryImpl implements PortfolioSnapshotRepository {

    private final PortfolioSnapshotJpaRepository jpaRepository;
    private final PortfolioSnapshotPersistenceMapper mapper;

    @Override
    public PortfolioSnapshot save(PortfolioSnapshot snapshot) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(snapshot)));
    }

    @Override
    public List<PortfolioSnapshot> findByUserIdAndSnapshotDateAfter(UserId userId, LocalDate from) {
        return jpaRepository.findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
                userId.value(), from)
                .stream().map(mapper::toDomain).toList();
    }
}
