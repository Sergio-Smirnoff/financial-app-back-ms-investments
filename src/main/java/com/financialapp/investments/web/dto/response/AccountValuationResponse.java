package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountValuationResponse {
    private String accountCbu;
    private String totalValuation;
    private String currency;
}
