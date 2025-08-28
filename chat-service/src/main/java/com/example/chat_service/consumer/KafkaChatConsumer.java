package com.example.chat_service.consumer;

import com.example.chat_service.entity.ChatMessage;
import com.example.chat_service.entity.ChatRoom;
import com.example.chat_service.event.ChatMessageEvent;
import com.example.chat_service.repository.ChatMessageRepository;
import com.example.chat_service.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaChatConsumer {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${kafka.topic.chat-messages}", groupId = "chat-service-group",containerFactory = "kafkaListenerContainerFactoryForMessageSent")
    @Transactional
    public void consumeChatMessage(ChatMessageEvent event) {
        try {
            log.info("Received chat message event: {}", event.getEventId());

            // Tìm hoặc tạo chat room
            ChatRoom chatRoom = chatRoomRepository
                    .findByStudentIdAndTutorId(
                            event.getSenderType().equals("STUDENT") ? event.getSenderId() : event.getReceiverId(),
                            event.getSenderType().equals("TUTOR") ? event.getSenderId() : event.getReceiverId()
                    )
                    .orElseGet(() -> {
                        ChatRoom newRoom = new ChatRoom();
                        newRoom.setStudentId(event.getReceiverType().equals("STUDENT") ? event.getReceiverId() : event.getSenderId());
                        newRoom.setTutorId(event.getReceiverType().equals("TUTOR") ? event.getReceiverId() : event.getSenderId());
                        return chatRoomRepository.save(newRoom);
                    });

            // Tạo và lưu message
            ChatMessage message = new ChatMessage();
            message.setSenderId(event.getSenderId());
            message.setContent(event.getContent());
            message.setChatRoom(chatRoom);
            message.setTimestamp(event.getTimestamp());

            ChatMessage savedMessage = chatMessageRepository.save(message);

            messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), savedMessage);

            log.info("Processed chat message: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage());
        }
    }
}