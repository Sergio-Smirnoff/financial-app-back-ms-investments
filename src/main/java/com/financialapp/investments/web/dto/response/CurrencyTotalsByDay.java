package com.financialapp.investments.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CurrencyTotalsByDay {
    private String currency;
    private BigDecimal totalValue;
}
