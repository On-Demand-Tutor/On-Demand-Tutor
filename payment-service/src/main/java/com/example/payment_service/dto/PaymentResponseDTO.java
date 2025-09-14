package com.example.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long studentId;
    private Long bookingId;
    private Double amount;
    private String status;
    private String transactionId;
    private String redirectUrl;
}