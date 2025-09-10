package com.example.notifications_service.consumer;

import com.example.notifications_service.event.PaymentLinkCreatedEvent;
import com.example.notifications_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;


    @KafkaListener(topics = "payment-link-events", groupId = "notifications-service-group",containerFactory = "kafkaListenerContainerFactoryForPaymentLink")
    public void handlePaymentLinkEvent(PaymentLinkCreatedEvent event) {
        log.info("Received payment link event: {}", event);
        log.info("Student email: {}", event.getEmail());

        emailService.sendPaymentLinkEmail(event.getEmail(), event);
    }
}
