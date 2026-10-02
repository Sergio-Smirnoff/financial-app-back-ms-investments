package com.financialapp.investments.web.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SellHoldingRequest {

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Digits(integer = 12, fraction = 6, message = "Quantity allows at most 12 integer digits and 6 decimals")
    private BigDecimal quantity;

    @Positive(message = "Price must be positive")
    @Digits(integer = 12, fraction = 6, message = "Price allows at most 12 integer digits and 6 decimals")
    private BigDecimal price;

    @Pattern(regexp = "\\d{22}", message = "destinationCbu must be exactly 22 digits")
    private String destinationCbu;
}
