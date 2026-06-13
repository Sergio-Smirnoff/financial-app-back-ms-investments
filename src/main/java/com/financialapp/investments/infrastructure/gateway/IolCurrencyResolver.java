package com.financialapp.investments.infrastructure.gateway;

import java.util.Locale;

public final class IolCurrencyResolver {

    private static final String ARS = "ARS";
    private static final String USD = "USD";

    private IolCurrencyResolver() {
    }

    public static String resolve(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            return ARS;
        }
        String normalized = moneda.toLowerCase(Locale.ROOT);
        if (normalized.contains("dolar")) {
            return USD;
        }
        return ARS;
    }
}
