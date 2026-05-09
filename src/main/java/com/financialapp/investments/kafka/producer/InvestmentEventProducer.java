package com.financialapp.investments.kafka.producer;

import com.financialapp.investments.kafka.event.InvestmentThresholdEvent;
import com.financialapp.investments.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvestmentEventProducer {

    private static final String TOPIC_THRESHOLD = "investment.threshold.reached";
    private static final String TOPIC_PAYMENT = "bank.payment.recorded";

    private final ApplicationEventPublisher eventPublisher;

    public void publishThresholdReached(InvestmentThresholdEvent event) {
        log.info("Queuing transactional investment.threshold.reached event for userId={}, ticker={}, direction={}",
                event.getUserId(), event.getPayload().getTicker(), event.getPayload().getDirection());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_THRESHOLD, String.valueOf(event.getUserId()), event));
    }

    public void publishPayment(PaymentEvent event) {
        log.info("Queuing transactional bank.payment.recorded for investment transaction, userId={}, amount={}",
                event.getUserId(), event.getAmount());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_PAYMENT, String.valueOf(event.getUserId()), event));
    }
}
