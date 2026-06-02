package com.financialapp.investments.web.mapper;

import java.math.BigDecimal;

final class BigDecimals {

    private BigDecimals() {}

    static String toPlain(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
}
