package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.NetPositionResult;
import com.financialapp.investments.domain.model.fee.TradeSide;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.service.BrokerFeeNetting;
import com.financialapp.investments.domain.usecase.holding.CreateHoldingUseCase;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateHoldingUseCaseImpl implements CreateHoldingUseCase {

    private final HoldingRepository holdingRepository;
    private final FinancesGateway financesGateway;
    private final DomainEventPublisher eventPublisher;
    private final BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @Override
    public Holding execute(CreateHoldingCommand command) {
        Holding holding = Holding.create(
                command.userId(),
                command.bankNumber(),
                command.ticker(),
                command.name(),
                command.assetType(),
                command.quantity(),
                command.avgPurchasePrice(),
                command.thresholdConfig()
        );
        Money totalCost = holding.costBasis();

        BrokerFeeSchedule schedule = brokerFeeScheduleRepository
                .findFor(command.bankNumber(), command.assetType())
                .orElse(null);
        NetPositionResult buyNet = new BrokerFeeNetting().apply(totalCost, totalCost, schedule, TradeSide.BUY);
        Money bookedAmount = buyNet.totalFee().amount().signum() > 0 ? totalCost.add(buyNet.totalFee()) : totalCost;

        if (command.fundingCbu() != null) {
            financesGateway.recordPurchase(command.userId(), command.fundingCbu(), bookedAmount);
        }

        Holding saved = holdingRepository.save(holding);

        eventPublisher.publish(new HoldingCreatedEvent(
                saved.id(), saved.userId(), saved.ticker(), saved.assetType(),
                saved.bankNumber(), command.fundingCbu(),
                saved.quantity(), saved.avgPurchasePrice(), totalCost,
                LocalDateTime.now()
        ));

        return saved;
    }
}
