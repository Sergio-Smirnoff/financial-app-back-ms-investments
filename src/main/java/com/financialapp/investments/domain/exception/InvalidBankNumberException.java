package com.financialapp.investments.domain.exception;

public class InvalidBankNumberException extends RuntimeException {

    public InvalidBankNumberException(String value) {
        super("bankNumber must be exactly 3 digits: " + value);
    }
}
