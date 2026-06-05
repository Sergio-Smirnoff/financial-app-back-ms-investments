package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class IolServiceException extends DomainException {

    public IolServiceException(String message) {
        super(DomainError.IOL_SERVICE_UNAVAILABLE, message);
    }

    public IolServiceException(String message, Throwable cause) {
        super(DomainError.IOL_SERVICE_UNAVAILABLE, message, cause);
    }
}
