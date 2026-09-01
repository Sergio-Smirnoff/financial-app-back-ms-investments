package com.financialapp.investments.web.dto.response;

import java.math.BigDecimal;

public record BrokerFeeScheduleResponse(
        Long id,
        String bankNumber,
        String assetType,
        BigDecimal buyFeePct,
        BigDecimal sellFeePct,
        BigDecimal minimumFee,
        BigDecimal marketFeePct,
        String ivaTreatment,
        String currency
) {}
