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
        message.setSubject("New Booking Created");
        message.setText("Bạn có booking mới với tutorId=" + event.getSkills()
                + " kỹ năng: " + event.getSkills()
                + " từ " + event.getStartTime() + " đến " + event.getEndTime());

        javaMailSender.send(message);
    }
}