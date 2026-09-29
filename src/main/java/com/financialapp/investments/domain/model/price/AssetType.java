package com.financialapp.investments.domain.model.price;

import java.math.BigDecimal;

public enum AssetType {
    STOCK,
    BOND,
    CEDEAR,
    FCI;

    public BigDecimal unitPrice(BigDecimal quote) {
        return switch (this) {
            case BOND -> quote.movePointLeft(2);
            case STOCK, CEDEAR, FCI -> quote;
        };
    }

    public BigDecimal marketValue(BigDecimal quote, BigDecimal quantity) {
        return unitPrice(quote).multiply(quantity);
    }
}
