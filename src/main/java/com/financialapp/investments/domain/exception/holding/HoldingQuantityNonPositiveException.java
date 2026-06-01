package com.financialapp.investments.domain.exception.holding;

import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.domain.exception.DomainException;

public class HoldingQuantityNonPositiveException extends DomainException {

    public HoldingQuantityNonPositiveException() {
        super(DomainError.HOLDING_QUANTITY_INVALID, "Holding quantity must be greater than zero");
    }
}
