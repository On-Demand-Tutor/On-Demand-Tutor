package com.example.notifications_service.service;

import com.example.notifications_service.event.PaymentLinkCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendPaymentLinkEmail(String to, PaymentLinkCreatedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Thanh toán buổi học #" + event.getBookingId());

        String body = "Xin chào,\n\n"
                + "Bạn đã đặt buổi học thành công.\n"
                + "📅 Thời gian: " + event.getStartTime() + " - " + event.getEndTime() + "\n"
                + "💡 Kỹ năng: " + event.getSkills() + "\n"
                + "💰 Giá: " + event.getPrice() + " VND\n\n"
                + "👉 Vui lòng bấm vào link dưới đây để thanh toán:\n"
                + event.getPaymentUrl();
        message.setText(body);
        javaMailSender.send(message);
    }
}
