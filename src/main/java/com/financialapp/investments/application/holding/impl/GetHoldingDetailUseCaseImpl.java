package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.NetPositionResult;
import com.financialapp.investments.domain.model.fee.TradeSide;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.PositionValuation;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.service.BrokerFeeNetting;
import com.financialapp.investments.domain.usecase.holding.GetHoldingDetailUseCase;
import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetHoldingDetailUseCaseImpl implements GetHoldingDetailUseCase {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final BrokerFeeScheduleRepository brokerFeeScheduleRepository;
    private final BrokerFeeNetting brokerFeeNetting;

    @Override
    public HoldingWithPriceResult execute(GetHoldingDetailCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        Optional<BigDecimal> quote = assetPriceRepository.findByTicker(holding.ticker())
                .map(AssetPrice::lastPrice);

        BrokerFeeSchedule schedule = brokerFeeScheduleRepository
                .findFor(holding.bankNumber(), holding.assetType())
                .orElse(null);

        return computeResult(holding, quote, schedule);
    }

    private HoldingWithPriceResult computeResult(
            Holding holding, Optional<BigDecimal> quote, BrokerFeeSchedule schedule) {
        Currency currency = holding.avgPurchasePrice().currency();
        PositionValuation gross = quote
                .map(price -> holding.valuation(new Money(price, currency)))
                .orElseGet(holding::valuationAtCost);

        NetPositionResult buyNet = brokerFeeNetting.apply(gross.costBasis(), gross.costBasis(), schedule, TradeSide.BUY);
        Money netCostBasis = buyNet.totalFee().amount().signum() > 0
                ? gross.costBasis().add(buyNet.totalFee())
                : gross.costBasis();

        NetPositionResult sellNet = brokerFeeNetting.apply(gross.marketValue(), gross.marketValue(), schedule, TradeSide.SELL);
        Money netMarketValue = sellNet.feeExceedsGross()
                ? Money.zero(currency.getCurrencyCode())
                : sellNet.netMagnitude();

        PositionValuation net = new PositionValuation(netMarketValue, netCostBasis);

        return new HoldingWithPriceResult(
                holding,
                quote.orElse(holding.avgPurchasePrice().amount()),
                gross.marketValue().amount(),
                net.profitAndLoss().amount(),
                net.profitAndLossPercent());
    }
}
