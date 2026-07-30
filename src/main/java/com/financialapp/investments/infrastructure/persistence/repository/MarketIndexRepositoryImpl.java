package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.domain.repository.MarketIndexRepository;
import com.financialapp.investments.infrastructure.persistence.entity.MarketIndexJpaEntity;
import com.financialapp.investments.infrastructure.persistence.mapper.MarketIndexPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MarketIndexRepositoryImpl implements MarketIndexRepository {

    private final MarketIndexJpaRepository jpaRepository;

    @Override
    public MarketIndex save(MarketIndex index) {
        MarketIndexJpaEntity entity = MarketIndexPersistenceMapper.toEntity(index);
        MarketIndexJpaEntity saved = jpaRepository.save(entity);
        return MarketIndexPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<MarketIndex> findByCode(String code) {
        return jpaRepository.findById(code).map(MarketIndexPersistenceMapper::toDomain);
    }

    @Override
    public List<MarketIndex> findAll() {
        return jpaRepository.findAll().stream()
                .map(MarketIndexPersistenceMapper::toDomain)
                .toList();
    }
}
