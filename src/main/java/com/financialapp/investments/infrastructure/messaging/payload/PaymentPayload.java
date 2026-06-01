package com.financialapp.investments.infrastructure.messaging.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPayload {

    private Long userId;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate date;
}
