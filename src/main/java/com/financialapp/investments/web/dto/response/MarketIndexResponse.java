package com.financialapp.investments.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MarketIndexResponse(
        String code,
        String value,
        @Schema(description = "Variation percentage for MERVAL/SP500, or absolute point delta for RIESGO_PAIS")
        String variation
) {}
