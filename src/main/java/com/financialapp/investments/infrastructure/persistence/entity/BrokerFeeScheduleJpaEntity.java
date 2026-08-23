package com.financialapp.investments.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "broker_fee_schedules", schema = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrokerFeeScheduleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_number", length = 3, nullable = false)
    private String bankNumber;

    @Column(name = "asset_type", length = 20)
    private String assetType;

    @Column(name = "buy_fee_pct", precision = 5, scale = 3)
    private BigDecimal buyFeePct;

    @Column(name = "sell_fee_pct", precision = 5, scale = 3)
    private BigDecimal sellFeePct;

    @Column(name = "minimum_fee", precision = 15, scale = 2)
    private BigDecimal minimumFee;

    @Column(name = "market_fee_pct", precision = 5, scale = 3)
    private BigDecimal marketFeePct;

    @Column(name = "iva_treatment", length = 10, nullable = false)
    private String ivaTreatment;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
}
