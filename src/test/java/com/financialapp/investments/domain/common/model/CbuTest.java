package com.financialapp.investments.domain.common.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CbuTest {

    @Test
    void accepts_22_digit_string() {
        Cbu cbu = new Cbu("0070009000000000000017");
        assertThat(cbu.value()).isEqualTo("0070009000000000000017");
    }

    @Test
    void rejects_non_22_digit() {
        assertThatThrownBy(() -> new Cbu("123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cbu(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
