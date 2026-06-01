package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.CloseHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.CloseHoldingUseCase;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.exception.BanksServiceException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.gateway.BanksGateway;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
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
    private final BanksGateway banksGateway;
    private final DomainEventPublisher eventPublisher;

    @Override
    public void execute(CloseHoldingCommand command) {
        Holding holding = holdingRepository
                .findByIdAndUserId(command.holdingId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found: " + command.holdingId().value()));

        Money unitPrice = assetPriceRepository.findByTicker(holding.ticker())
                .map(ap -> Money.of(ap.lastPrice(), ap.currency()))
                .orElse(holding.avgPurchasePrice());

        Money proceeds = unitPrice.multiply(holding.quantity().value());

        if (command.destinationAccountId() != null) {
            try {
                banksGateway.adjustBalance(command.destinationAccountId(), proceeds);
            } catch (InfrastructureException e) {
                throw new BanksServiceException("Failed to credit destination account on holding close", e);
            }
        }

        holdingRepository.delete(holding.id());

        eventPublisher.publish(new HoldingClosedEvent(
                holding.id(), holding.userId(), holding.ticker(),
                holding.bankAccountId(), command.destinationAccountId(),
                proceeds, LocalDateTime.now()
        ));
    }
}
