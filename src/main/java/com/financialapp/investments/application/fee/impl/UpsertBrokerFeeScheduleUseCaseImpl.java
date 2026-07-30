package com.financialapp.investments.application.fee.impl;

import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.usecase.fee.UpsertBrokerFeeSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpsertBrokerFeeScheduleUseCaseImpl implements UpsertBrokerFeeSchedule {

    private final BrokerFeeScheduleRepository repository;

    @Override
    public BrokerFeeSchedule execute(UpsertBrokerFeeScheduleCommand command) {
        if (command == null || command.bankNumber() == null) {
            throw new IllegalArgumentException("Bank number must not be null");
        }
        BrokerFeeSchedule schedule = new BrokerFeeSchedule(
                null,
                command.bankNumber(),
                command.assetType(),
                command.buyFeePct(),
                command.sellFeePct(),
                command.minimumFee(),
                command.marketFeePct(),
                command.ivaTreatment()
        );
        return repository.save(schedule);
    }
}
