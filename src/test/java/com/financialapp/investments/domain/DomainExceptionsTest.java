package com.financialapp.investments.domain;

import com.financialapp.investments.domain.exception.BanksServiceException;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.investments.domain.exception.ResourceConflictException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.exception.UnsupportedCurrencyException;
import com.financialapp.investments.domain.exception.holding.HoldingCurrencyMismatchException;
import com.financialapp.investments.domain.exception.holding.HoldingQuantityNonPositiveException;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

    @Test
    void resourceNotFound_mapsError() {
        ResourceNotFoundException e = new ResourceNotFoundException("missing");
        assertThat(e.getError()).isEqualTo(DomainError.RESOURCE_NOT_FOUND);
        assertThat(e.getMessage()).isEqualTo("missing");
    }

    @Test
    void resourceAlreadyExists_mapsError() {
        ResourceAlreadyExistsException e = new ResourceAlreadyExistsException("dup");
        assertThat(e.getError()).isEqualTo(DomainError.RESOURCE_ALREADY_EXISTS);
        assertThat(e.getMessage()).isEqualTo("dup");
    }

    @Test
    void resourceConflict_mapsError() {
        ResourceConflictException e = new ResourceConflictException("conflict");
        assertThat(e.getError()).isEqualTo(DomainError.RESOURCE_CONFLICT);
    }

    @Test
    void iolService_messageOnly_constructor() {
        IolServiceException e = new IolServiceException("io fail");
        assertThat(e.getError()).isEqualTo(DomainError.IOL_SERVICE_UNAVAILABLE);
        assertThat(e.getCause()).isNull();
    }

    @Test
    void iolService_withCause_constructor() {
        Throwable cause = new RuntimeException("root");
        IolServiceException e = new IolServiceException("wrapped", cause);
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void banksService_messageOnly_constructor() {
        BanksServiceException e = new BanksServiceException("banks down");
        assertThat(e.getError()).isEqualTo(DomainError.BANKS_SERVICE_UNAVAILABLE);
        assertThat(e.getCause()).isNull();
    }

    @Test
    void banksService_withCause_constructor() {
        Throwable cause = new RuntimeException("root");
        BanksServiceException e = new BanksServiceException("wrapped", cause);
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void unsupportedCurrency_listsAllowedSorted() {
        UnsupportedCurrencyException e = new UnsupportedCurrencyException(
                "EUR", Set.of(Currency.getInstance("ARS"), Currency.getInstance("USD")));
        assertThat(e.getError()).isEqualTo(DomainError.UNSUPPORTED_CURRENCY);
        assertThat(e.getMessage()).contains("EUR").contains("[ARS, USD]");
    }

    @Test
    void holdingCurrencyMismatch_formatsExpectedActual() {
        HoldingCurrencyMismatchException e = new HoldingCurrencyMismatchException("ARS", "USD");
        assertThat(e.getError()).isEqualTo(DomainError.HOLDING_CURRENCY_MISMATCH);
        assertThat(e.getMessage()).contains("ARS").contains("USD");
    }

    @Test
    void holdingQuantityNonPositive_defaultMessage() {
        HoldingQuantityNonPositiveException e = new HoldingQuantityNonPositiveException();
        assertThat(e.getError()).isEqualTo(DomainError.HOLDING_QUANTITY_INVALID);
        assertThat(e.getMessage()).contains("greater than zero");
    }
}
