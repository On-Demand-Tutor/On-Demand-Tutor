package com.example.payment_service.service;


import com.example.payment_service.dto.PaymentRequestDTO;
import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.event.PaymentSuccessEvent;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public boolean handleCallback(Map<String, String> params) {
        Map<String, String> fields = new HashMap<>(params);
        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String calculated = com.example.payment_service.util.VNPayUtil.hashAllFields(fields, vnPayService.getVnp_HashSecret());
        if (!Objects.equals(vnp_SecureHash, calculated)) {
            return false;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");

        Payment payment = paymentRepository.findByBookingId(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + txnRef));

        if ("00".equals(responseCode)) {
            payment.setStatus("SUCCESS");
            payment.setTransactionNo(transactionNo);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus("FAILED");
        }

        paymentRepository.save(payment);

        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .studentId(payment.getStudentId())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();

        kafkaTemplate.send("payment-events", event);

        return true;
    }
}
