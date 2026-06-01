package com.financialapp.investments.domain.exception;

public abstract class DomainException extends RuntimeException {

    private final DomainError error;

    protected DomainException(DomainError error, String message) {
        super(message);
        this.error = error;
    }

    protected DomainException(DomainError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public DomainError error() {
        return error;
    }
}
