package com.financialapp.investments.service;

import com.financialapp.investments.client.BanksClient;
import com.financialapp.investments.exception.ResourceNotFoundException;
import com.financialapp.investments.kafka.event.PaymentEvent;
import com.financialapp.investments.kafka.producer.InvestmentEventProducer;
import com.financialapp.investments.mapper.HoldingMapper;
import com.financialapp.investments.model.dto.request.HoldingRequest;
import com.financialapp.investments.model.dto.response.AccountValuationResponse;
import com.financialapp.investments.model.dto.response.HoldingResponse;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceRepository;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final HoldingMapper holdingMapper;
    private final BanksClient banksClient;
    private final InvestmentEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<HoldingResponse> list(Long userId) {
        return holdingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(holdingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<HoldingResponse> list(Long userId, Pageable pageable) {
        return holdingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(holdingMapper::toResponse);
    }

    @CacheEvict(value = "portfolio", key = "#userId")
    @Transactional
    public HoldingResponse create(Long userId, HoldingRequest request) {
        BigDecimal totalCost = request.getQuantity().multiply(request.getAvgPurchasePrice());
        
        // 1. Deduct funds from funding account (Fail-fast)
        if (request.getFundingAccountId() != null) {
            banksClient.adjustBalance(request.getFundingAccountId(), totalCost.negate(), request.getCurrency());
            
            // Record transaction in funding account (Debit/Expense)
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(request.getFundingAccountId())
                    .amount(totalCost)
                    .currency(request.getCurrency())
                    .description("Investment Buy: " + request.getTicker())
                    .date(LocalDate.now())
                    .build());

            // Record transaction in investment account (Credit/Income)
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(request.getBankAccountId())
                    .amount(totalCost.negate())
                    .currency(request.getCurrency())
                    .description("Investment Buy: " + request.getTicker())
                    .date(LocalDate.now())
                    .build());
        }

        Holding holding = Holding.builder()
                .userId(userId)
                .bankAccountId(request.getBankAccountId())
                .bankId(request.getBankId())
                .ticker(request.getTicker().toUpperCase())
                .name(request.getName())
                .assetType(AssetType.valueOf(request.getAssetType()))
                .quantity(request.getQuantity())
                .avgPurchasePrice(request.getAvgPurchasePrice())
                .currency(request.getCurrency())
                .notifyGainThresholdPct(request.getNotifyGainThresholdPct())
                .notifyLossThresholdPct(request.getNotifyLossThresholdPct())
                .build();
        
        Holding saved = holdingRepository.save(holding);
        log.info("Created holding id={} ticker={} for userId={}", saved.getId(), saved.getTicker(), userId);
        return holdingMapper.toResponse(saved);
    }

    @CacheEvict(value = "portfolio", key = "#userId")
    @Transactional
    public HoldingResponse update(Long id, Long userId, HoldingRequest request) {
        Holding holding = findOwnedHolding(id, userId);

        // Calculate quantity difference for balance adjustment
        BigDecimal qtyDiff = request.getQuantity().subtract(holding.getQuantity());
        if (qtyDiff.compareTo(BigDecimal.ZERO) != 0 && request.getFundingAccountId() != null) {
            BigDecimal costDiff = qtyDiff.multiply(request.getAvgPurchasePrice());
            // If qty increased (costDiff > 0), deduct from account. If decreased, credit.
            banksClient.adjustBalance(request.getFundingAccountId(), costDiff.negate(), request.getCurrency());

            // Record transaction in funding account
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(request.getFundingAccountId())
                    .amount(costDiff)
                    .currency(request.getCurrency())
                    .description("Investment Update (" + (costDiff.signum() > 0 ? "Buy" : "Sell") + "): " + holding.getTicker())
                    .date(LocalDate.now())
                    .build());

            // Record transaction in investment account (inverse of funding)
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(request.getBankAccountId())
                    .amount(costDiff.negate())
                    .currency(request.getCurrency())
                    .description("Investment Update (" + (costDiff.signum() > 0 ? "Buy" : "Sell") + "): " + holding.getTicker())
                    .date(LocalDate.now())
                    .build());
        }

        holding.setBankAccountId(request.getBankAccountId());
        holding.setBankId(request.getBankId());
        holding.setTicker(request.getTicker().toUpperCase());
        holding.setName(request.getName());
        holding.setAssetType(AssetType.valueOf(request.getAssetType()));
        holding.setQuantity(request.getQuantity());
        holding.setAvgPurchasePrice(request.getAvgPurchasePrice());
        holding.setCurrency(request.getCurrency());
        holding.setNotifyGainThresholdPct(request.getNotifyGainThresholdPct());
        holding.setNotifyLossThresholdPct(request.getNotifyLossThresholdPct());
        
        return holdingMapper.toResponse(holdingRepository.save(holding));
    }

    @Transactional(readOnly = true)
    public long countByAccountId(Long accountId) {
        return holdingRepository.countByBankAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public AccountValuationResponse getAccountValuation(Long accountId) {
        List<Holding> holdings = holdingRepository.findByBankAccountId(accountId);
        if (holdings.isEmpty()) {
            return new AccountValuationResponse(accountId, BigDecimal.ZERO, null);
        }

        String currency = holdings.get(0).getCurrency();
        BigDecimal total = holdings.stream()
                .map(h -> {
                    BigDecimal price = assetPriceRepository.findByTicker(h.getTicker())
                            .map(AssetPrice::getLastPrice)
                            .orElse(h.getAvgPurchasePrice());
                    return h.getQuantity().multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountValuationResponse(accountId, total, currency);
    }

    @CacheEvict(value = "portfolio", key = "#userId")
    @Transactional
    public void delete(Long id, Long userId, Long destinationAccountId) {
        Holding holding = findOwnedHolding(id, userId);
        
        if (destinationAccountId != null) {
            BigDecimal marketPrice = assetPriceRepository.findByTicker(holding.getTicker())
                    .map(AssetPrice::getLastPrice)
                    .orElse(holding.getAvgPurchasePrice());
            BigDecimal liquidationValue = holding.getQuantity().multiply(marketPrice);

            // Credit destination account
            banksClient.adjustBalance(destinationAccountId, liquidationValue, holding.getCurrency());

            // Record transaction in destination account (Credit/Income)
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(destinationAccountId)
                    .amount(liquidationValue.negate())
                    .currency(holding.getCurrency())
                    .description("Investment Sell: " + holding.getTicker())
                    .date(LocalDate.now())
                    .build());

            // Record transaction in investment account (Debit/Expense)
            eventProducer.publishPayment(PaymentEvent.builder()
                    .userId(userId)
                    .accountId(holding.getBankAccountId())
                    .amount(liquidationValue)
                    .currency(holding.getCurrency())
                    .description("Investment Sell: " + holding.getTicker())
                    .date(LocalDate.now())
                    .build());
        }

        holdingRepository.delete(holding);
        log.info("Liquidated holding id={} for userId={}", id, userId);
    }

    private Holding findOwnedHolding(Long id, Long userId) {
        return holdingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", id));
    }
}
