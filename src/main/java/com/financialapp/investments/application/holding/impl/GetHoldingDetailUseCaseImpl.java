package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.holding.GetHoldingDetailUseCase;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetHoldingDetailUseCaseImpl implements GetHoldingDetailUseCase {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @Override
    public HoldingWithPriceResult execute(GetHoldingDetailCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        BigDecimal currentPrice = assetPriceRepository.findByTicker(holding.ticker())
                .map(ap -> ap.lastPrice())
                .orElse(holding.avgPurchasePrice().amount());

        com.financialapp.investments.domain.model.fee.BrokerFeeSchedule schedule = brokerFeeScheduleRepository
                .findFor(holding.bankNumber(), holding.assetType())
                .orElse(null);

        return computeResult(holding, currentPrice, schedule);
    }

    private static HoldingWithPriceResult computeResult(
            Holding holding,
            BigDecimal currentPrice,
            com.financialapp.investments.domain.model.fee.BrokerFeeSchedule schedule) {

        BigDecimal quantity = holding.quantity().value();
        BigDecimal avgPrice = holding.avgPurchasePrice().amount();
        String currency = holding.avgPurchasePrice().currency().getCurrencyCode();

        com.financialapp.investments.domain.common.model.Money costBasisMoney = com.financialapp.investments.domain.common.model.Money.of(avgPrice.multiply(quantity), currency);
        com.financialapp.investments.domain.common.model.Money currentValueMoney = com.financialapp.investments.domain.common.model.Money.of(currentPrice.multiply(quantity), currency);

        com.financialapp.investments.domain.service.BrokerFeeNetting feeNetting = new com.financialapp.investments.domain.service.BrokerFeeNetting();

        com.financialapp.investments.domain.model.fee.NetPositionResult buyNet = feeNetting.apply(costBasisMoney, costBasisMoney, schedule, com.financialapp.investments.domain.model.fee.TradeSide.BUY);
        com.financialapp.investments.domain.common.model.Money netCostBasis = buyNet.totalFee().amount().compareTo(BigDecimal.ZERO) > 0
                ? costBasisMoney.add(buyNet.totalFee())
                : costBasisMoney;

        com.financialapp.investments.domain.model.fee.NetPositionResult sellNet = feeNetting.apply(currentValueMoney, currentValueMoney, schedule, com.financialapp.investments.domain.model.fee.TradeSide.SELL);
        com.financialapp.investments.domain.common.model.Money netCurrentValue = sellNet.feeExceedsGross()
                ? com.financialapp.investments.domain.common.model.Money.zero(currency)
                : sellNet.netMagnitude();

        BigDecimal plAmount = netCurrentValue.amount().subtract(netCostBasis.amount());
        BigDecimal costBasisVal = netCostBasis.amount();
        BigDecimal plPercent = costBasisVal.compareTo(BigDecimal.ZERO) != 0
                ? plAmount.divide(costBasisVal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new HoldingWithPriceResult(holding, currentPrice, currentValueMoney.amount(), plAmount, plPercent);
    }
}
