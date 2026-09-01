package com.financialapp.investments.domain.model.fx;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FxSnapshotRates(
        BigDecimal mepRate,
        BigDecimal cclRate,
        BigDecimal oficialRate,
        LocalDate rateDate
) {}
