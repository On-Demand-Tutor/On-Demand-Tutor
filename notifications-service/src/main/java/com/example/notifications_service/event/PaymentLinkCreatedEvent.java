package com.example.notifications_service.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLinkCreatedEvent {
    private Long bookingId;
    private Long studentId;
    private Long tutorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
    private String skills;
    private String email;
    private double price;
    private String paymentUrl;
}
