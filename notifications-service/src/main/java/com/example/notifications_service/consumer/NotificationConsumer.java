package com.example.notifications_service.consumer;

import com.example.notifications_service.event.BookingEvent;
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

    @KafkaListener(topics = "booking-events", groupId = "notifications-service-group",containerFactory = "kafkaListenerContainerFactoryForBookingTutor")
    public void handleBookingEvent(BookingEvent event) {
        log.info("Received booking event: {}", event);
        log.info("Student email: {}", event.getEmail());
        emailService.sendBookingNotification(event.getEmail(), event);
    }
}
