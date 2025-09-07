package com.example.chat_service.controller;

import com.example.chat_service.entity.ChatMessage;
import com.example.chat_service.entity.ChatRoom;
import com.example.chat_service.repository.ChatMessageRepository;
import com.example.chat_service.repository.ChatRoomRepository;
import com.example.chat_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable String roomId,
            @Payload ChatMessage chatMessage) {

        try {
            // Validate message content
            if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }

            // Kiểm tra room tồn tại
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Chat room not found: " + roomId));

            // Set timestamp và room
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setChatRoom(chatRoom);

            // Lưu vào database
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // Gửi đến tất cả clients trong room
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);

            log.info("Message sent to room {}: {}", roomId, savedMessage.getId());

        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{targetUserId}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long currentUserId = jwt.getClaim("userId");
        String role = jwt.getClaim("scope");

        List<ChatMessage> messages = chatService.getMessagesByUser(currentUserId, role, targetUserId);
        return ResponseEntity.ok(messages);
    }

}