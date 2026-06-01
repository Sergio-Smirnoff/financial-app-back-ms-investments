package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountValuationResponse {
    private Long accountId;
    private BigDecimal totalValuation;
    private String currency;
}
