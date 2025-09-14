package com.example.payment_service.controller;


import com.example.payment_service.entity.Payment;
import com.example.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // VNPay gọi về (GET)
    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> callback(HttpServletRequest req) {
        Map<String, String[]> paramMap = req.getParameterMap();
        Map<String, String> params = new HashMap<>();
        paramMap.forEach((k, v) -> params.put(k, v[0]));

        boolean ok = paymentService.handleCallback(params);
        if (!ok) return ResponseEntity.badRequest().body("Invalid signature");
        return ResponseEntity.ok("OK Đã thanh toán rồi giờ mày gửi event qua bên booking mà cập nhật đi =====>>>>>>>>");
    }

    @GetMapping("/getAllPayment")
    public Page<Payment> getAllPayments(
            @RequestParam int page,
            @RequestParam int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentService.getAllPayments(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
