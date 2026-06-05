package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class UnsupportedCurrencyException extends DomainException {

    public UnsupportedCurrencyException(String code, Set<Currency> allowed) {
        super(DomainError.UNSUPPORTED_CURRENCY,
                "Currency " + code + " is not supported. Allowed: "
                        + allowed.stream()
                                .map(Currency::getCurrencyCode)
                                .sorted()
                                .collect(Collectors.toList()));
    }
}
