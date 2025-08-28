package com.example.chat_service.event;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
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