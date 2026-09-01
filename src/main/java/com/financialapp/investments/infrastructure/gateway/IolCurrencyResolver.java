package com.financialapp.investments.infrastructure.gateway;

import java.util.Locale;

public final class IolCurrencyResolver {

    private static final String ARS = "ARS";
    private static final String USD = "USD";

    private IolCurrencyResolver() {
    }

    public static String resolve(String currency) {
        if (currency == null || currency.isBlank()) {
            return ARS;
        }
        String normalized = currency.toLowerCase(Locale.ROOT);
        if (normalized.contains("dolar")) {
            return USD;
        }
        return ARS;
    }
}
