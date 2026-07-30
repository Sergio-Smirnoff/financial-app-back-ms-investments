package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.infrastructure.persistence.entity.BrokerFeeScheduleJpaEntity;
import com.financialapp.investments.infrastructure.persistence.mapper.BrokerFeeSchedulePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BrokerFeeScheduleRepositoryImpl implements BrokerFeeScheduleRepository {

    private final BrokerFeeScheduleJpaRepository jpaRepository;

    @Override
    public BrokerFeeSchedule save(BrokerFeeSchedule schedule) {
        String bankNum = schedule.bankNumber().value();
        String assetTypeStr = schedule.assetType() != null ? schedule.assetType().name() : null;

        Optional<BrokerFeeScheduleJpaEntity> existing = jpaRepository.findByBankNumberAndAssetType(bankNum, assetTypeStr);
        BrokerFeeScheduleJpaEntity entity = BrokerFeeSchedulePersistenceMapper.toEntity(schedule);
        existing.ifPresent(e -> entity.setId(e.getId()));

        BrokerFeeScheduleJpaEntity saved = jpaRepository.save(entity);
        return BrokerFeeSchedulePersistenceMapper.toDomain(saved);
    }

    @Override
    public List<BrokerFeeSchedule> findAll() {
        return jpaRepository.findAll().stream()
                .map(BrokerFeeSchedulePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<BrokerFeeSchedule> findFor(BankNumber bankNumber, AssetType assetType) {
        if (bankNumber == null) return Optional.empty();
        String assetTypeStr = assetType != null ? assetType.name() : null;
        return jpaRepository.findForBankAndAssetType(bankNumber.value(), assetTypeStr)
                .map(BrokerFeeSchedulePersistenceMapper::toDomain);
    }
}
