package com.example.chat_service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
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