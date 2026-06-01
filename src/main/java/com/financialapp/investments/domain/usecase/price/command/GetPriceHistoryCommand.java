package com.financialapp.investments.domain.usecase.price.command;

import com.financialapp.investments.domain.model.holding.Ticker;

import java.time.LocalDateTime;

public record GetPriceHistoryCommand(
        Ticker ticker,
        LocalDateTime from,
        LocalDateTime to
) {}
