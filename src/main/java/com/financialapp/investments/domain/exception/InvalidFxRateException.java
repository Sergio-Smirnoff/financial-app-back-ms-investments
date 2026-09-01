package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class InvalidFxRateException extends DomainException {

    public InvalidFxRateException(String message) {
        super(DomainError.INVALID_FX_RATE, message);
    }
}
