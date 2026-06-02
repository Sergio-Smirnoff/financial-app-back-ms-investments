package com.financialapp.investments.domain.exception;

public class FinancesServiceException extends RuntimeException {

    public FinancesServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
