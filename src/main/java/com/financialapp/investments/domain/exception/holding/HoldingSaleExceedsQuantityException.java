package com.financialapp.investments.domain.exception.holding;

import com.financialapp.commons.core.error.DomainException;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;

public class HoldingSaleExceedsQuantityException extends DomainException {

    public HoldingSaleExceedsQuantityException(HoldingQuantity sold, HoldingQuantity held) {
        super(DomainError.HOLDING_SALE_EXCEEDS_QUANTITY,
                "Cannot sell " + sold.value().stripTrailingZeros().toPlainString()
                        + " units of a holding of " + held.value().stripTrailingZeros().toPlainString());
    }
}
