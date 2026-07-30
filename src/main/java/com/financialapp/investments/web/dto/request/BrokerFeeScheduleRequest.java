package com.financialapp.investments.web.dto.request;

import java.math.BigDecimal;

public class BrokerFeeScheduleRequest {
    private String assetType;
    private BigDecimal buyFeePct;
    private BigDecimal sellFeePct;
    private BigDecimal minimumFee;
    private BigDecimal marketFeePct;
    private String ivaTreatment;
    private String currency;

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public BigDecimal getBuyFeePct() { return buyFeePct; }
    public void setBuyFeePct(BigDecimal buyFeePct) { this.buyFeePct = buyFeePct; }
    public BigDecimal getSellFeePct() { return sellFeePct; }
    public void setSellFeePct(BigDecimal sellFeePct) { this.sellFeePct = sellFeePct; }
    public BigDecimal getMinimumFee() { return minimumFee; }
    public void setMinimumFee(BigDecimal minimumFee) { this.minimumFee = minimumFee; }
    public BigDecimal getMarketFeePct() { return marketFeePct; }
    public void setMarketFeePct(BigDecimal marketFeePct) { this.marketFeePct = marketFeePct; }
    public String getIvaTreatment() { return ivaTreatment; }
    public void setIvaTreatment(String ivaTreatment) { this.ivaTreatment = ivaTreatment; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
