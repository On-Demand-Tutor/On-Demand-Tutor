package com.example.student_service.event;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatMessageEvent {
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageEvent() {
        this.timestamp = LocalDateTime.now();
    }
}