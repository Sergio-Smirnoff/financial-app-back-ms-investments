package com.financialapp.investments.application.fee.impl;

import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.usecase.fee.ListBrokerFeeSchedules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListBrokerFeeSchedulesUseCaseImpl implements ListBrokerFeeSchedules {

    private final BrokerFeeScheduleRepository repository;

    @Override
    public List<BrokerFeeSchedule> execute() {
        return repository.findAll();
    }
}
