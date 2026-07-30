package com.financialapp.investments.domain.exception;

public class InvalidFxRateException extends RuntimeException {

    public InvalidFxRateException(String message) {
        super(message);
    }
}
