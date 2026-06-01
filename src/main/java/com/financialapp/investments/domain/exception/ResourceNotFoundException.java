package com.financialapp.investments.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(DomainError.RESOURCE_NOT_FOUND, message);
    }
}
