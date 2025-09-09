package com.example.notifications_service.service;

import com.example.notifications_service.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendBookingNotification(String to, BookingEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("📌 Thông báo Booking mới");

        String body = "Bạn có một booking mới:\n\n"
                + "📅 Thời gian: " + event.getStartTime() + " - " + event.getEndTime() + "\n"
                + "✅ Trạng thái: " + event.getStatus() + "\n"
                + "🕒 Ngày tạo: " + event.getCreatedAt() + "\n"
                + "💡 Kỹ năng: " + event.getSkills() + "\n";

        message.setText(body);

        javaMailSender.send(message);
    }
}
