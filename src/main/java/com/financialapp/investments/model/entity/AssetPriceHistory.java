package com.financialapp.investments.model.entity;

import com.financialapp.investments.model.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_price_history", schema = "investments",
        indexes = @Index(name = "idx_aph_ticker_priced_at", columnList = "ticker, priced_at"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(name = "last_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal lastPrice;

    @Column(name = "open_price", precision = 18, scale = 6)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 18, scale = 6)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 6)
    private BigDecimal lowPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal volume;

    @Column(name = "daily_variation", precision = 8, scale = 4)
    private BigDecimal dailyVariation;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "priced_at", nullable = false)
    private LocalDateTime pricedAt;
}
