package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class BanksServiceException extends DomainException {

    public BanksServiceException(String message) {
        super(DomainError.BANKS_SERVICE_UNAVAILABLE, message);
    }

    public BanksServiceException(String message, Throwable cause) {
        super(DomainError.BANKS_SERVICE_UNAVAILABLE, message, cause);
    }
}
