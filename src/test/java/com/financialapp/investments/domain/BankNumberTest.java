package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.exception.InvalidBankNumberException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankNumberTest {

    @Test
    void accepts_three_digit_code() {
        assertThat(new BankNumber("007").value()).isEqualTo("007");
    }

    @Test
    void extracts_from_cbu_prefix() {
        assertThat(BankNumber.fromCbu("0070099530000012345678").value()).isEqualTo("007");
    }

    @Test
    void rejects_non_three_digits() {
        assertThatThrownBy(() -> new BankNumber("7")).isInstanceOf(InvalidBankNumberException.class);
        assertThatThrownBy(() -> new BankNumber("0070")).isInstanceOf(InvalidBankNumberException.class);
        assertThatThrownBy(() -> new BankNumber("abc")).isInstanceOf(InvalidBankNumberException.class);
        assertThatThrownBy(() -> new BankNumber(null)).isInstanceOf(InvalidBankNumberException.class);
    }
}
