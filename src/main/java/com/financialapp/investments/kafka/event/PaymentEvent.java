package com.financialapp.investments.kafka.event;

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
public class PaymentEvent {
    private Long userId;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate date;
}
