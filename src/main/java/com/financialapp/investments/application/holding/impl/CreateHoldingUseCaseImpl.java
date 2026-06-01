package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.CreateHoldingUseCase;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.exception.BanksServiceException;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.gateway.BanksGateway;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateHoldingUseCaseImpl implements CreateHoldingUseCase {

    private final HoldingRepository holdingRepository;
    private final BanksGateway banksGateway;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Holding execute(CreateHoldingCommand command) {
        Money totalCost = command.avgPurchasePrice().multiply(command.quantity().value());

        if (command.fundingAccountId() != null) {
            try {
                banksGateway.adjustBalance(command.fundingAccountId(), totalCost.negate());
            } catch (InfrastructureException e) {
                throw new BanksServiceException("Failed to debit funding account for holding purchase", e);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Holding holding = new Holding(
                new HoldingId(null),
                command.userId(),
                command.bankAccountId(),
                command.bankId(),
                command.ticker(),
                command.name(),
                command.assetType(),
                command.quantity(),
                command.avgPurchasePrice(),
                command.thresholdConfig(),
                NotificationTimestamps.empty(),
                now,
                now
        );

        Holding saved = holdingRepository.save(holding);

        eventPublisher.publish(new HoldingCreatedEvent(
                saved.id(), saved.userId(), saved.ticker(), saved.assetType(),
                saved.bankAccountId(), command.fundingAccountId(),
                saved.quantity(), saved.avgPurchasePrice(), totalCost,
                LocalDateTime.now()
        ));

        return saved;
    }
}
