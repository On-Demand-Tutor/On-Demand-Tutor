package com.example.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {
    private Long paymentId;
    private Long bookingId;
    private Long studentId;
    private Double amount;
    private LocalDateTime paidAt;
}
