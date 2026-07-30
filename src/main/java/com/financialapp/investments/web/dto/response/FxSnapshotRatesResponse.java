package com.financialapp.investments.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FxSnapshotRatesResponse(
        String mepRate,
        String cclRate,
        String oficialRate,
        LocalDate rateDate
) {}
