package com.financialapp.investments.domain.exception;

public class ResourceConflictException extends DomainException {

    public ResourceConflictException(String message) {
        super(DomainError.RESOURCE_CONFLICT, message);
    }
}
