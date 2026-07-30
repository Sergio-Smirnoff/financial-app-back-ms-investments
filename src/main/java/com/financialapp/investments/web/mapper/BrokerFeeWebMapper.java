package com.financialapp.investments.web.mapper;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.fee.UpsertBrokerFeeSchedule.UpsertBrokerFeeScheduleCommand;
import com.financialapp.investments.web.dto.request.BrokerFeeScheduleRequest;
import com.financialapp.investments.web.dto.response.BrokerFeeScheduleResponse;

public final class BrokerFeeWebMapper {

    private BrokerFeeWebMapper() {}

    public static UpsertBrokerFeeScheduleCommand toCommand(String bankNumber, BrokerFeeScheduleRequest request) {
        AssetType assetType = request.getAssetType() != null && !request.getAssetType().isBlank()
                ? AssetType.valueOf(request.getAssetType().toUpperCase())
                : null;

        String currencyStr = request.getCurrency() != null && !request.getCurrency().isBlank()
                ? request.getCurrency()
                : "ARS";

        Money minFee = request.getMinimumFee() != null
                ? Money.of(request.getMinimumFee(), currencyStr)
                : null;

        IvaTreatment ivaTreatment = request.getIvaTreatment() != null && !request.getIvaTreatment().isBlank()
                ? IvaTreatment.valueOf(request.getIvaTreatment().toUpperCase())
                : IvaTreatment.SEPARATE;

        return new UpsertBrokerFeeScheduleCommand(
                new BankNumber(bankNumber),
                assetType,
                request.getBuyFeePct(),
                request.getSellFeePct(),
                minFee,
                request.getMarketFeePct(),
                ivaTreatment
        );
    }

    public static BrokerFeeScheduleResponse toResponse(BrokerFeeSchedule schedule) {
        if (schedule == null) return null;
        return new BrokerFeeScheduleResponse(
                schedule.id() != null ? schedule.id().value() : null,
                schedule.bankNumber().value(),
                schedule.assetType() != null ? schedule.assetType().name() : null,
                schedule.buyFeePct(),
                schedule.sellFeePct(),
                schedule.minimumFee() != null ? schedule.minimumFee().amount() : null,
                schedule.marketFeePct(),
                schedule.ivaTreatment() != null ? schedule.ivaTreatment().name() : null,
                schedule.minimumFee() != null ? schedule.minimumFee().currency().getCurrencyCode() : "ARS"
        );
    }
}
