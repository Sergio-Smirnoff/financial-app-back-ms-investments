package com.financialapp.investments.domain.usecase.fee;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.price.AssetType;

import java.math.BigDecimal;

public interface UpsertBrokerFeeSchedule {
    record UpsertBrokerFeeScheduleCommand(
            BankNumber bankNumber,
            AssetType assetType,
            BigDecimal buyFeePct,
            BigDecimal sellFeePct,
            Money minimumFee,
            BigDecimal marketFeePct,
            IvaTreatment ivaTreatment
    ) {}

    BrokerFeeSchedule execute(UpsertBrokerFeeScheduleCommand command);
}
