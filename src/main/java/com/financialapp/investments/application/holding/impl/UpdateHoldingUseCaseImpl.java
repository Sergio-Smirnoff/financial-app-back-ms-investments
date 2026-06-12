package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.UpdateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.UpdateHoldingUseCase;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.gateway.FinancesGateway;
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
    private final FinancesGateway financesGateway;
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

        if (command.fundingCbu() != null && costDifference.amount().signum() != 0) {
            if (costDifference.amount().signum() > 0) {
                financesGateway.recordPurchase(command.userId(), command.fundingCbu(), costDifference);
            } else {
                financesGateway.recordSaleProceeds(command.userId(), command.fundingCbu(), costDifference.negate());
            }
        }

        Holding updated = new Holding(
                existing.id(),
                existing.userId(),
                command.bankNumber(),
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
                saved.bankNumber(), command.fundingCbu(),
                saved.quantity(), existing.quantity(),
                saved.avgPurchasePrice(), costDifference,
                LocalDateTime.now()
        ));

        return saved;
    }
}
