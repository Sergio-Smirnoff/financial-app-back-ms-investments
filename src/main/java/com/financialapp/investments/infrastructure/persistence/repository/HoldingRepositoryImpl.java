package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.HoldingJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.HoldingPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HoldingRepositoryImpl implements HoldingRepository, HoldingQueryGateway {

    private final HoldingJpaRepository jpaRepository;
    private final HoldingPersistenceMapper mapper;

    @Override
    public Holding save(Holding holding) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(holding)));
    }

    @Override
    public List<Holding> saveAll(List<Holding> holdings) {
        return jpaRepository.saveAll(holdings.stream().map(mapper::toEntity).toList())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Holding> findByIdAndUserId(HoldingId id, UserId userId) {
        return jpaRepository.findByIdAndUserId(id.value(), userId.value())
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Holding> findFiltered(HoldingFilter filter, PageRequest pageRequest) {
        var springPage = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size());
        var result = filter.assetType() != null
                ? jpaRepository.findByUserIdAndAssetTypeOrderByCreatedAtDesc(
                        filter.userId().value(), filter.assetType(), springPage)
                : jpaRepository.findByUserIdOrderByCreatedAtDesc(filter.userId().value(), springPage);
        List<Holding> content = result.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public List<Holding> findByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(HoldingId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public List<UserId> findDistinctUserIds() {
        return jpaRepository.findDistinctUserIds().stream()
                .map(UserId::new).toList();
    }

    @Override
    public List<Ticker> findDistinctTickers() {
        return jpaRepository.findDistinctTickers().stream()
                .map(Ticker::new).toList();
    }

    @Override
    public List<Holding> findWithThresholds() {
        return jpaRepository.findHoldingsWithThresholds().stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Holding> findFirstByTicker(Ticker ticker) {
        return jpaRepository.findFirstByTicker(ticker.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Holding> findByBankAccountId(BanksAccountId accountId) {
        return jpaRepository.findByBankAccountId(accountId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByBankAccountId(BanksAccountId accountId) {
        return jpaRepository.countByBankAccountId(accountId.value());
    }
}
