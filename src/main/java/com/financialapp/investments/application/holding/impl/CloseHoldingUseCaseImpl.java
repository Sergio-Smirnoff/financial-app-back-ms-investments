package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.NetPositionResult;
import com.financialapp.investments.domain.model.fee.TradeSide;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.service.BrokerFeeNetting;
import com.financialapp.investments.domain.usecase.holding.CloseHoldingUseCase;
import com.financialapp.investments.domain.usecase.holding.command.CloseHoldingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CloseHoldingUseCaseImpl implements CloseHoldingUseCase {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final FinancesGateway financesGateway;
    private final DomainEventPublisher eventPublisher;
    private final BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @Override
    public void execute(CloseHoldingCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        Money proceeds = assetPriceRepository.findByTicker(holding.ticker())
                .map(price -> holding.marketValue(Money.of(price.lastPrice(), price.currency())))
                .orElseGet(holding::costBasis);

        BrokerFeeSchedule schedule = brokerFeeScheduleRepository
                .findFor(holding.bankNumber(), holding.assetType())
                .orElse(null);
        NetPositionResult sellNet = new BrokerFeeNetting().apply(proceeds, proceeds, schedule, TradeSide.SELL);
        Money bookedAmount = sellNet.feeExceedsGross()
                ? Money.zero(proceeds.currency().getCurrencyCode())
                : sellNet.netMagnitude();

        if (command.destinationCbu() != null) {
            financesGateway.recordSaleProceeds(command.userId(), command.destinationCbu(), bookedAmount);
        }

        holdingRepository.delete(holding.id());

        eventPublisher.publish(new HoldingClosedEvent(
                holding.id(), holding.userId(), holding.ticker(),
                holding.bankNumber(), command.destinationCbu(),
                proceeds, LocalDateTime.now()
        ));
    }
}
