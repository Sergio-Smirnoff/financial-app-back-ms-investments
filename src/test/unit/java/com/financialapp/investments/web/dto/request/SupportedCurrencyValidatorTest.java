package com.financialapp.investments.web.dto.request;

import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportedCurrencyValidatorTest {

    @Mock
    SupportedCurrencies supported;

    SupportedCurrencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SupportedCurrencyValidator(supported);
    }

    @Test
    void nullCode_isValid_relyOnNotBlankElsewhere() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void supportedCode_isValid() {
        when(supported.isSupported(Currency.getInstance("ARS"))).thenReturn(true);

        assertThat(validator.isValid("ARS", null)).isTrue();
    }

    @Test
    void unsupportedButValidIsoCode_isInvalid() {
        when(supported.isSupported(Currency.getInstance("BRL"))).thenReturn(false);

        assertThat(validator.isValid("BRL", null)).isFalse();
    }

    @Test
    void notIsoCode_isInvalid_noException() {
        assertThat(validator.isValid("XYZ_NOT_ISO", null)).isFalse();
    }

    @Test
    void registryWiderThanSupported_respectsWhitelist() {
        Set<Currency> allowed = Set.of(Currency.getInstance("ARS"), Currency.getInstance("USD"));
        when(supported.isSupported(any(Currency.class)))
                .thenAnswer(inv -> allowed.contains(inv.getArgument(0)));

        assertThat(validator.isValid("ARS", null)).isTrue();
        assertThat(validator.isValid("USD", null)).isTrue();
        assertThat(validator.isValid("EUR", null)).isFalse();
        assertThat(validator.isValid("JPY", null)).isFalse();
    }
}
