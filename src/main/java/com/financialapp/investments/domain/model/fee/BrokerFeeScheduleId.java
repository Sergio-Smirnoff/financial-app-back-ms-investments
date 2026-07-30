package com.financialapp.investments.domain.model.fee;

public record BrokerFeeScheduleId(Long value) {
    public BrokerFeeScheduleId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("BrokerFeeScheduleId must be positive");
        }
    }
}
