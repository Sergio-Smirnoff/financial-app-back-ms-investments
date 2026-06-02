package com.financialapp.investments.domain.usecase.price.command;

import com.financialapp.investments.domain.model.holding.Ticker;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class GetPriceHistoryCommandTest {

    private static final Ticker TIC = new Ticker("GGAL");
    private static final LocalDateTime FROM = LocalDateTime.of(2026, 5, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 6, 1, 0, 0);

    @Test
    void rejectsNullFrom() {
        assertThatNullPointerException()
                .isThrownBy(() -> new GetPriceHistoryCommand(TIC, null, TO));
    }

    @Test
    void rejectsNullTo() {
        assertThatNullPointerException()
                .isThrownBy(() -> new GetPriceHistoryCommand(TIC, FROM, null));
    }
}
