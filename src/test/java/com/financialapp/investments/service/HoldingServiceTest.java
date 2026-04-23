package com.financialapp.investments.service;

import com.financialapp.investments.client.BanksClient;
import com.financialapp.investments.kafka.event.PaymentEvent;
import com.financialapp.investments.kafka.producer.InvestmentEventProducer;
import com.financialapp.investments.mapper.HoldingMapper;
import com.financialapp.investments.model.dto.request.HoldingRequest;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceRepository;
import com.financialapp.investments.repository.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private AssetPriceRepository assetPriceRepository;
    @Mock
    private HoldingMapper holdingMapper;
    @Mock
    private BanksClient banksClient;
    @Mock
    private InvestmentEventProducer eventProducer;

    @InjectMocks
    private HoldingService holdingService;

    private Long userId = 1L;
    private Long bankAccountId = 10L;
    private Long fundingAccountId = 20L;
    private String ticker = "AAPL";

    @Test
    void create_ShouldPublishEventsWithCorrectSigns() {
        HoldingRequest request = new HoldingRequest();
        request.setBankAccountId(bankAccountId);
        request.setFundingAccountId(fundingAccountId);
        request.setTicker(ticker);
        request.setQuantity(new BigDecimal("10"));
        request.setAvgPurchasePrice(new BigDecimal("150"));
        request.setCurrency("USD");
        request.setAssetType("STOCK");

        BigDecimal totalCost = new BigDecimal("1500");

        when(holdingRepository.save(any(Holding.class))).thenAnswer(i -> i.getArguments()[0]);

        holdingService.create(userId, request);

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventProducer, times(2)).publishPayment(eventCaptor.capture());

        PaymentEvent fundingEvent = eventCaptor.getAllValues().get(0);
        assertThat(fundingEvent.getAccountId()).isEqualTo(fundingAccountId);
        // Expect negative for expense in funding account
        assertThat(fundingEvent.getAmount()).isEqualByComparingTo(totalCost.negate());

        PaymentEvent investmentEvent = eventCaptor.getAllValues().get(1);
        assertThat(investmentEvent.getAccountId()).isEqualTo(bankAccountId);
        // Expect positive for income/asset increase in investment account
        assertThat(investmentEvent.getAmount()).isEqualByComparingTo(totalCost);
    }

    @Test
    void update_ShouldPublishEventsWithCorrectSigns_WhenQuantityIncreases() {
        Holding holding = Holding.builder()
                .id(1L)
                .userId(userId)
                .bankAccountId(bankAccountId)
                .ticker(ticker)
                .quantity(new BigDecimal("5"))
                .avgPurchasePrice(new BigDecimal("100"))
                .currency("USD")
                .build();

        HoldingRequest request = new HoldingRequest();
        request.setBankAccountId(bankAccountId);
        request.setFundingAccountId(fundingAccountId);
        request.setTicker(ticker);
        request.setQuantity(new BigDecimal("10")); // Increase by 5
        request.setAvgPurchasePrice(new BigDecimal("100"));
        request.setCurrency("USD");
        request.setAssetType("STOCK");

        when(holdingRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(holding));
        
        BigDecimal costDiff = new BigDecimal("500");

        holdingService.update(1L, userId, request);

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventProducer, times(2)).publishPayment(eventCaptor.capture());

        PaymentEvent fundingEvent = eventCaptor.getAllValues().get(0);
        assertThat(fundingEvent.getAmount()).isEqualByComparingTo(costDiff.negate());

        PaymentEvent investmentEvent = eventCaptor.getAllValues().get(1);
        assertThat(investmentEvent.getAmount()).isEqualByComparingTo(costDiff);
    }

    @Test
    void delete_ShouldPublishEventsWithCorrectSigns() {
        Holding holding = Holding.builder()
                .id(1L)
                .userId(userId)
                .bankAccountId(bankAccountId)
                .ticker(ticker)
                .quantity(new BigDecimal("10"))
                .avgPurchasePrice(new BigDecimal("150"))
                .currency("USD")
                .build();

        Long destinationAccountId = 30L;
        when(holdingRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(ticker)).thenReturn(Optional.of(
                AssetPrice.builder().lastPrice(new BigDecimal("160")).build()
        ));

        BigDecimal liquidationValue = new BigDecimal("1600");

        holdingService.delete(1L, userId, destinationAccountId);

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventProducer, times(2)).publishPayment(eventCaptor.capture());

        PaymentEvent destinationEvent = eventCaptor.getAllValues().get(0);
        assertThat(destinationEvent.getAccountId()).isEqualTo(destinationAccountId);
        // Expect positive for income in destination account
        assertThat(destinationEvent.getAmount()).isEqualByComparingTo(liquidationValue);

        PaymentEvent investmentEvent = eventCaptor.getAllValues().get(1);
        assertThat(investmentEvent.getAccountId()).isEqualTo(bankAccountId);
        // Expect negative for expense/asset decrease in investment account
        assertThat(investmentEvent.getAmount()).isEqualByComparingTo(liquidationValue.negate());
    }
}
