// service/VNPayService.java
package com.example.payment_service.service;

import com.example.payment_service.util.VNPayUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@Service
public class VNPayService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    public String buildPaymentUrl(String txnRef, Double amount , String clientIp) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // nhân 100 theo spec
        vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_TxnRef", txnRef);

        // sort và build hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                String encoded = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=').append(encoded).append('&');
                query.append(fieldName).append('=').append(encoded).append('&');
            }
        }
        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        String secureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnp_PayUrl + "?" + query;
    }
}
