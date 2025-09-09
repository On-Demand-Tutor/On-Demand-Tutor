package com.example.payment_service.controller;


import com.example.payment_service.dto.PaymentRequestDTO;
import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/vnpay/create")
    public ResponseEntity<PaymentResponseDTO> create(
            @RequestBody PaymentRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpReq
    ) {
        return ResponseEntity.ok(paymentService.createPayment(request, jwt, httpReq));
    }

    // VNPay gọi về (GET)
    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> callback(HttpServletRequest req) {
        Map<String, String[]> paramMap = req.getParameterMap();
        Map<String, String> params = new HashMap<>();
        paramMap.forEach((k, v) -> params.put(k, v[0]));

        boolean ok = paymentService.handleCallback(params);
        if (!ok) return ResponseEntity.badRequest().body("Invalid signature");
        return ResponseEntity.ok("OK");
    }
}
