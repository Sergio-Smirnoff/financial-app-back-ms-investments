package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class InfrastructureException extends DomainException {

    public InfrastructureException(String message) {
        super(DomainError.INTERNAL_ERROR, message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(DomainError.INTERNAL_ERROR, message, cause);
    }
}
