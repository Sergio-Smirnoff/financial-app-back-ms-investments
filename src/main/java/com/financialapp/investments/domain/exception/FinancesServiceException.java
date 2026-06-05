package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class FinancesServiceException extends DomainException {

    public FinancesServiceException(String message, Throwable cause) {
        super(DomainError.FINANCES_SERVICE_UNAVAILABLE, message, cause);
    }
}
