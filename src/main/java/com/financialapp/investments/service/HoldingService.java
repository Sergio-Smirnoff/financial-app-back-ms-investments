package com.financialapp.investments.service;

import com.financialapp.investments.exception.ResourceNotFoundException;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final HoldingMapper holdingMapper;

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
        Holding holding = Holding.builder()
                .userId(userId)
                .bankAccountId(request.getBankAccountId())
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
        holding.setBankAccountId(request.getBankAccountId());
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
    public void delete(Long id, Long userId) {
        Holding holding = findOwnedHolding(id, userId);
        holdingRepository.delete(holding);
        log.info("Deleted holding id={} for userId={}", id, userId);
    }

    private Holding findOwnedHolding(Long id, Long userId) {
        return holdingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", id));
    }
}
