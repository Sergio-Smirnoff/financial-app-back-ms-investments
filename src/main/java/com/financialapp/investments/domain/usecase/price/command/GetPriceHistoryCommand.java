package com.financialapp.investments.domain.usecase.price.command;

import com.financialapp.investments.domain.model.holding.Ticker;

import java.time.LocalDateTime;
import java.util.Objects;

public record GetPriceHistoryCommand(
        Ticker ticker,
        LocalDateTime from,
        LocalDateTime to
) {
    public GetPriceHistoryCommand {
        Objects.requireNonNull(ticker, "ticker");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
    }
}
