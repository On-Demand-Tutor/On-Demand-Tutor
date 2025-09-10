package com.example.book_service.consumer;

import com.example.book_service.enums.BookingStatus;
import com.example.book_service.event.PaymentSuccessEvent;
import com.example.book_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingConsumer {

    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "payment-success-events", groupId = "booking-service-group",containerFactory = "paymentSuccessEventConcurrentKafkaListenerContainer")
    public void handleBookingSuccessEvent(PaymentSuccessEvent paymentSuccessEvent) {
        log.info("Nhận được PaymentSuccessEvent: {}", paymentSuccessEvent);

        bookingRepository.findById(paymentSuccessEvent.getBookingId())
                .ifPresentOrElse(booking -> {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                    log.info("Booking {} đã được CONFIRMED", booking.getId());
                }, () -> log.error("Không tìm thấy booking với id {}", paymentSuccessEvent.getBookingId()));
    }
}
