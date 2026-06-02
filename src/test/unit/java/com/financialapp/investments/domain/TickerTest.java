package com.financialapp.investments.domain;

import com.financialapp.investments.domain.model.holding.Ticker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TickerTest {

    @Test
    void nullValue_throws() {
        assertThatThrownBy(() -> new Ticker(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void emptyValue_throws() {
        assertThatThrownBy(() -> new Ticker(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void specialCharsOnly_throws() {
        assertThatThrownBy(() -> new Ticker("$$$"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tooLong_throws() {
        assertThatThrownBy(() -> new Ticker("A".repeat(21)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lowercase_normalizedToUpperCase() {
        Ticker t = new Ticker("aapl");
        assertThat(t.value()).isEqualTo("AAPL");
    }

    @Test
    void validAlphanumeric_accepted() {
        Ticker t = new Ticker("VALE3");
        assertThat(t.value()).isEqualTo("VALE3");
    }

    @Test
    void dotAllowed_accepted() {
        Ticker t = new Ticker("BRK.B");
        assertThat(t.value()).isEqualTo("BRK.B");
    }
}
