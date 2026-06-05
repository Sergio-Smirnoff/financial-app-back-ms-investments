package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(DomainError.RESOURCE_NOT_FOUND, message);
    }
}
