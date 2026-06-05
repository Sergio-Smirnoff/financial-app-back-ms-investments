package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class ResourceAlreadyExistsException extends DomainException {

    public ResourceAlreadyExistsException(String message) {
        super(DomainError.RESOURCE_ALREADY_EXISTS, message);
    }
}
