package com.financialapp.investments.domain.model.holding;

import java.util.Objects;

public record BanksAccountId(Long value) {

    public BanksAccountId {
        Objects.requireNonNull(value, "banksAccountId must not be null");
    }
}
