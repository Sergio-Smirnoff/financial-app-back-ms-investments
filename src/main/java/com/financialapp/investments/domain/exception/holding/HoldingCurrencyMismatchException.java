package com.financialapp.investments.domain.exception.holding;

import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;

public class HoldingCurrencyMismatchException extends DomainException {

    public HoldingCurrencyMismatchException(String expected, String actual) {
        super(DomainError.HOLDING_CURRENCY_MISMATCH,
                "Currency mismatch: expected " + expected + " but got " + actual);
    }
}
