package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.NetPositionResult;
import com.financialapp.investments.domain.model.fee.TradeSide;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.service.BrokerFeeNetting;
import com.financialapp.investments.domain.usecase.holding.SellHoldingUseCase;
import com.financialapp.investments.domain.usecase.holding.command.SellHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.response.HoldingSaleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SellHoldingUseCaseImpl implements SellHoldingUseCase {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final FinancesGateway financesGateway;
    private final DomainEventPublisher eventPublisher;
    private final BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @Override
    public HoldingSaleResult execute(SellHoldingCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserIdForUpdate(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        HoldingQuantity sold = command.quantity() != null ? command.quantity() : holding.quantity();
        Holding remaining = holding.isFullySoldBy(sold) ? null : holding.afterSelling(sold);

        Money proceeds = proceedsOf(holding, sold, command.manualQuote());
        Money booked = netOfBrokerFees(holding, proceeds);

        if (command.destinationCbu() != null) {
            financesGateway.recordSaleProceeds(command.userId(), command.destinationCbu(), booked);
        }

        LocalDateTime now = LocalDateTime.now();
        if (remaining == null) {
            holdingRepository.delete(holding.id());
            eventPublisher.publish(new HoldingClosedEvent(
                    holding.id(), holding.userId(), holding.ticker(),
                    holding.bankNumber(), command.destinationCbu(),
                    proceeds, now));
            return new HoldingSaleResult(proceeds, booked, sold, null);
        }

        Holding saved = holdingRepository.save(remaining);
        eventPublisher.publish(new HoldingUpdatedEvent(
                saved.id(), saved.userId(), saved.ticker(),
                saved.bankNumber(), command.destinationCbu(),
                saved.quantity(), holding.quantity(),
                saved.avgPurchasePrice(), saved.costBasis().subtract(holding.costBasis()),
                now));
        return new HoldingSaleResult(proceeds, booked, sold, saved.quantity());
    }

    private Money proceedsOf(Holding holding, HoldingQuantity sold, BigDecimal manualQuote) {
        if (manualQuote != null) {
            return holding.saleProceeds(new Money(manualQuote, holding.avgPurchasePrice().currency()), sold);
        }
        return assetPriceRepository.findByTicker(holding.ticker())
                .map(price -> holding.saleProceeds(Money.of(price.lastPrice(), price.currency()), sold))
                .orElseGet(() -> holding.avgPurchasePrice().multiply(sold.value()));
    }

    private Money netOfBrokerFees(Holding holding, Money proceeds) {
        BrokerFeeSchedule schedule = brokerFeeScheduleRepository
                .findFor(holding.bankNumber(), holding.assetType())
                .orElse(null);
        NetPositionResult sellNet = new BrokerFeeNetting().apply(proceeds, proceeds, schedule, TradeSide.SELL);
        return sellNet.feeExceedsGross()
                ? Money.zero(proceeds.currency().getCurrencyCode())
                : sellNet.netMagnitude();
    }
}
