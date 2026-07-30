package com.financialapp.investments.domain.usecase.fee;

import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;

import java.util.List;

public interface ListBrokerFeeSchedules {
    List<BrokerFeeSchedule> execute();
}
