package com.example.student_service.event;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatMessageEvent {
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageEvent() {
        this.timestamp = LocalDateTime.now();
    }
}