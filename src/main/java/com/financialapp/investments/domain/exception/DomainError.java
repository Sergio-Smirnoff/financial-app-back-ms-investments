package com.financialapp.investments.domain.exception;

import com.financialapp.commons.core.error.ErrorCategory;
import com.financialapp.commons.core.error.ErrorCode;

public enum DomainError implements ErrorCode {

    RESOURCE_NOT_FOUND(ErrorCategory.NOT_FOUND, "resource_not_found"),
    RESOURCE_ALREADY_EXISTS(ErrorCategory.CONFLICT, "resource_already_exists"),
    RESOURCE_CONFLICT(ErrorCategory.CONFLICT, "resource_conflict"),
    HOLDING_QUANTITY_INVALID(ErrorCategory.UNPROCESSABLE, "holding_quantity_invalid"),
    HOLDING_CURRENCY_MISMATCH(ErrorCategory.UNPROCESSABLE, "holding_currency_mismatch"),
    UNSUPPORTED_CURRENCY(ErrorCategory.UNPROCESSABLE, "unsupported_currency"),
    IOL_SERVICE_UNAVAILABLE(ErrorCategory.INTERNAL_SERVER_ERROR, "iol_service_unavailable"),
    FINANCES_SERVICE_UNAVAILABLE(ErrorCategory.INTERNAL_SERVER_ERROR, "finances_service_unavailable"),
    BANKS_SERVICE_UNAVAILABLE(ErrorCategory.INTERNAL_SERVER_ERROR, "banks_service_unavailable"),
    INTERNAL_ERROR(ErrorCategory.INTERNAL_SERVER_ERROR, "internal_error");

    private final ErrorCategory category;
    private final String code;

    DomainError(ErrorCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    @Override
    public ErrorCategory category() { return category; }

    @Override
    public String code() { return code; }
}
