package com.example.chat_service.repository;

import com.example.chat_service.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
}
