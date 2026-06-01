package com.financialapp.investments.domain.common.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

//TODO: check error handling with messages
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, Currency.getInstance(currency));
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, Currency.getInstance(currency));
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
}
