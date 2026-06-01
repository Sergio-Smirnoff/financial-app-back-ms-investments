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

    @Override
    public HoldingWithPriceResult execute(GetHoldingDetailCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        BigDecimal currentPrice = assetPriceRepository.findByTicker(holding.ticker())
                .map(ap -> ap.lastPrice())
                .orElse(holding.avgPurchasePrice().amount());

        return computeResult(holding, currentPrice);
    }

    private static HoldingWithPriceResult computeResult(Holding holding, BigDecimal currentPrice) {
        BigDecimal quantity = holding.quantity().value();
        BigDecimal avgPrice = holding.avgPurchasePrice().amount();
        BigDecimal currentValue = currentPrice.multiply(quantity);
        BigDecimal costBasis = avgPrice.multiply(quantity);
        BigDecimal plAmount = currentValue.subtract(costBasis);
        BigDecimal plPercent = costBasis.compareTo(BigDecimal.ZERO) != 0
                ? plAmount.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new HoldingWithPriceResult(holding, currentPrice, currentValue, plAmount, plPercent);
    }
}
