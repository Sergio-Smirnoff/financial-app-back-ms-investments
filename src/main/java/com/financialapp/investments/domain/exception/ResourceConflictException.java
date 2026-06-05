package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class ResourceConflictException extends DomainException {

    public ResourceConflictException(String message) {
        super(DomainError.RESOURCE_CONFLICT, message);
    }
}
