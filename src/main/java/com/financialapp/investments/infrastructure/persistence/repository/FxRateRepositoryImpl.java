package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.infrastructure.persistence.entity.FxRateJpaEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.FxRateJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.FxRatePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FxRateRepositoryImpl implements FxRateRepository {

    private final FxRateJpaRepository jpaRepository;
    private final FxRatePersistenceMapper mapper;

    @Override
    public FxRate save(FxRate rate) {
        Optional<FxRateJpaEntity> existing = jpaRepository.findByRateDateAndFxView(rate.date(), rate.view());
        FxRateJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setBuy(rate.buy());
            entity.setSell(rate.sell());
            entity.setSource(rate.source());
        } else {
            entity = mapper.toEntity(rate);
        }
        FxRateJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FxRate> findByDate(LocalDate date, FxView view) {
        return jpaRepository.findByRateDateAndFxView(date, view).map(mapper::toDomain);
    }

    @Override
    public List<FxRate> findByDateBetween(LocalDate from, LocalDate to, FxView view) {
        return jpaRepository.findByRateDateBetweenAndFxViewOrderByRateDateAsc(from, to, view)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FxRate> findLatest(FxView view) {
        return jpaRepository.findFirstByFxViewOrderByRateDateDesc(view).map(mapper::toDomain);
    }
}
