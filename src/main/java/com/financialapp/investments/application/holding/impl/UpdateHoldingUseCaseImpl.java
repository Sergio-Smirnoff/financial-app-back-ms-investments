package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.UpdateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.UpdateHoldingUseCase;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.exception.BanksServiceException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.holding.Holding;
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
public class UpdateHoldingUseCaseImpl implements UpdateHoldingUseCase {

    private final HoldingRepository holdingRepository;
    private final BanksGateway banksGateway;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Holding execute(UpdateHoldingCommand command) {
        Holding existing = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        Money oldTotalCost = existing.avgPurchasePrice().multiply(existing.quantity().value());
        Money newTotalCost = command.newAvgPurchasePrice().multiply(command.newQuantity().value());
        Money costDifference = newTotalCost.subtract(oldTotalCost);

        try {
            banksGateway.adjustBalance(command.fundingAccountId(), costDifference.negate());
        } catch (InfrastructureException e) {
            throw new BanksServiceException("Failed to adjust funding account for holding update", e);
        }

        Holding updated = new Holding(
                existing.id(),
                existing.userId(),
                command.bankAccountId(),
                command.bankId(),
                existing.ticker(),
                command.name(),
                existing.assetType(),
                command.newQuantity(),
                command.newAvgPurchasePrice(),
                command.thresholdConfig(),
                existing.notificationTimestamps(),
                existing.createdAt(),
                LocalDateTime.now()
        );

        Holding saved = holdingRepository.save(updated);

        eventPublisher.publish(new HoldingUpdatedEvent(
                saved.id(), saved.userId(), saved.ticker(),
                saved.bankAccountId(), command.fundingAccountId(),
                saved.quantity(), existing.quantity(),
                saved.avgPurchasePrice(), costDifference,
                LocalDateTime.now()
        ));

        return saved;
    }
}
