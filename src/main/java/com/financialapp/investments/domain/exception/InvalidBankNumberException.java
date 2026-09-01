package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class InvalidBankNumberException extends DomainException {

    public InvalidBankNumberException(String value) {
        super(DomainError.INVALID_BANK_NUMBER, "bankNumber must be exactly 3 digits: " + value);
    }
}
