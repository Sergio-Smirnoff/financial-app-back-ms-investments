package com.financialapp.investments.web.dto.request;

import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Currency;

@RequiredArgsConstructor
public class SupportedCurrencyValidator implements ConstraintValidator<SupportedCurrency, String> {

    private final SupportedCurrencies supported;

    @Override
    public boolean isValid(String code, ConstraintValidatorContext ctx) {
        if (code == null) return true;
        try {
            return supported.isSupported(Currency.getInstance(code));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
