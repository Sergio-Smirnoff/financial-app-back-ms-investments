package com.financialapp.investments.domain.exception;

public enum DomainError {

    RESOURCE_NOT_FOUND(404, "resource_not_found"),
    RESOURCE_ALREADY_EXISTS(409, "resource_already_exists"),
    RESOURCE_CONFLICT(409, "resource_conflict"),
    HOLDING_QUANTITY_INVALID(422, "holding_quantity_invalid"),
    HOLDING_CURRENCY_MISMATCH(422, "holding_currency_mismatch"),
    UNSUPPORTED_CURRENCY(422, "unsupported_currency"),
    IOL_SERVICE_UNAVAILABLE(500, "iol_service_unavailable"),
    BANKS_SERVICE_UNAVAILABLE(500, "banks_service_unavailable"),
    INTERNAL_ERROR(500, "internal_error");

    private final int httpStatusCode;
    private final String code;

    DomainError(int httpStatusCode, String code) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
    }

    public int httpStatusCode() {
        return httpStatusCode;
    }

    public String code() {
        return code;
    }
}
