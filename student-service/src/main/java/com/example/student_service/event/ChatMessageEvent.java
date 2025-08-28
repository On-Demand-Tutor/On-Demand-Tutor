package com.example.student_service.event;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatMessageEvent {
    private String eventId;
    private String senderId;
    private String senderType;
    private String senderName;
    private String receiverId;
    private String receiverType;
    private String content;
    private LocalDateTime timestamp;
    private String chatRoomId;

    public ChatMessageEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
}