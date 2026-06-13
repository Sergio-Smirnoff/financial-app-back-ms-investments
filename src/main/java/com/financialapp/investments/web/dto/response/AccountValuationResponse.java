package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountValuationResponse {
    private String bankNumber;
    private String totalValuation;
    private String currency;
}
