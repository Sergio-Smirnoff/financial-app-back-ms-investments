package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrencyTotalsByDay {
    private String currency;
    private String totalValue;
}
