package com.financialapp.investments.web.dto.response;

import java.time.LocalDate;

public record FxRateResponse(
        LocalDate date,
        String view,
        String buy,
        String sell,
        String source
) {}
