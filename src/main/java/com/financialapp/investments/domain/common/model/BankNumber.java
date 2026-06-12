package com.financialapp.investments.domain.common.model;

import com.financialapp.investments.domain.exception.InvalidBankNumberException;

import java.util.regex.Pattern;

public record BankNumber(String value) {

    private static final Pattern BANK_CODE_FORMAT = Pattern.compile("^\\d{3}$");

    public BankNumber {
        if (value == null || !BANK_CODE_FORMAT.matcher(value).matches()) {
            throw new InvalidBankNumberException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
