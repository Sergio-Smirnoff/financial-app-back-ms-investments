package com.financialapp.investments.domain.common.model;

import java.util.regex.Pattern;

public record Cbu(String value) {

    private static final Pattern CBU_PATTERN = Pattern.compile("\\d{22}");

    public Cbu {
        if (value == null || !CBU_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("cbu must be exactly 22 digits: " + value);
        }
    }
}
