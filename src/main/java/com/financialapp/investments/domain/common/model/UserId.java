package com.financialapp.investments.domain.common.model;

import java.util.Objects;

//value object
public record UserId(Long value) {

    public UserId {
        Objects.requireNonNull(value, "userId must not be null");
    }
}
