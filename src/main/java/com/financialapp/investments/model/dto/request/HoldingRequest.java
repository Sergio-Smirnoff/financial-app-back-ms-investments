package com.financialapp.investments.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class HoldingRequest {

    @NotBlank(message = "Ticker is required")
    @Size(max = 20, message = "Ticker must not exceed 20 characters")
    private String ticker;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Asset type is required")
    @Pattern(regexp = "STOCK|BOND|CEDEAR|FCI", message = "Asset type must be STOCK, BOND, CEDEAR, or FCI")
    private String assetType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Average purchase price is required")
    @PositiveOrZero(message = "Average purchase price must be zero or positive")
    private BigDecimal avgPurchasePrice;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "ARS|USD", message = "Currency must be ARS or USD")
    private String currency;

    @PositiveOrZero(message = "Gain threshold must be zero or positive")
    private BigDecimal notifyGainThresholdPct;

    @PositiveOrZero(message = "Loss threshold must be zero or positive")
    private BigDecimal notifyLossThresholdPct;
}
