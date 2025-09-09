package com.example.payment_service.consum;


import com.example.payment_service.entity.Payment;
import com.example.payment_service.event.BookingEvent;
import com.example.payment_service.event.PaymentLinkCreatedEvent;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsume {
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "booking-events", groupId = "payment-service-group",containerFactory = "paymentKafkaListenerContainerFactoryForBookingTutor")
    public void handleBookingEvent(BookingEvent bookingEvent) {
        log.info("Nhận booking-event: {}", bookingEvent);

        Payment payment = Payment.builder()
                .bookingId(bookingEvent.getBookingId())
                .studentId(bookingEvent.getStudentId())
                .amount(bookingEvent.getPrice())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        String paymentUrl = vnPayService.buildPaymentUrl(
                payment.getId().toString(),
                bookingEvent.getPrice(),
                "127.0.0.1"
        );

        // Gửi event chứa link sang notifications-service
        PaymentLinkCreatedEvent linkEvent = PaymentLinkCreatedEvent.builder()
                .bookingId(bookingEvent.getBookingId())
                .studentId(bookingEvent.getStudentId())
                .tutorId(bookingEvent.getTutorId())
                .startTime(bookingEvent.getStartTime())
                .endTime(bookingEvent.getEndTime())
                .status(bookingEvent.getStatus())
                .createdAt(bookingEvent.getCreatedAt())
                .skills(bookingEvent.getSkills())
                .email(bookingEvent.getEmail())
                .price(bookingEvent.getPrice())
                .paymentUrl(paymentUrl)
                .build();

        kafkaTemplate.send("payment-link-events", linkEvent);


        log.info("Đã tạo payment {} và gửi link sang notification-service", payment.getId());
    }
}
