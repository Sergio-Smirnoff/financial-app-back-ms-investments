package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.price.AssetType;

import java.util.List;
import java.util.Optional;

public interface BrokerFeeScheduleRepository {
    BrokerFeeSchedule save(BrokerFeeSchedule schedule);
    List<BrokerFeeSchedule> findAll();
    Optional<BrokerFeeSchedule> findFor(BankNumber bankNumber, AssetType assetType);
}
