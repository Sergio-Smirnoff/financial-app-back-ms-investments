package com.financialapp.investments.domain.exception;

public class ResourceAlreadyExistsException extends DomainException {

    public ResourceAlreadyExistsException(String message) {
        super(DomainError.RESOURCE_ALREADY_EXISTS, message);
    }
}
