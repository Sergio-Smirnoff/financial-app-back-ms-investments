package com.financialapp.investments.domain.gateway;

import java.util.Currency;
import java.util.Set;

public interface SupportedCurrencies {

    boolean isSupported(Currency currency);

    Set<Currency> all();
}
