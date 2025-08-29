package com.example.chat_service.consumer;

import com.example.chat_service.entity.ChatMessage;
import com.example.chat_service.entity.ChatRoom;
import com.example.chat_service.event.ChatMessageEvent;
import com.example.chat_service.repository.ChatMessageRepository;
import com.example.chat_service.repository.ChatRoomRepository;
import com.example.chat_service.service.ChatService;
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

    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${kafka.topic.chat-messages}",
            groupId = "chat-service-group",
            containerFactory = "kafkaListenerContainerFactoryForMessageSent"
    )
    @Transactional
    public void consume(ChatMessageEvent event) {
        log.info("Nhận chat event: {}", event);

        boolean senderOk = chatService.verifyStudent(event.getSenderId())
                || chatService.verifyTutor(event.getSenderId());

        boolean receiverOk = chatService.verifyStudent(event.getReceiverId())
                || chatService.verifyTutor(event.getReceiverId());

        if (!(senderOk && receiverOk)) {
            log.warn("Xác thực thất bại: senderOk={}, receiverOk={}", senderOk, receiverOk);
            return;
        }


        // 🔹 Tìm hoặc tạo ChatRoom (giữa 2 user bất kỳ)
        ChatRoom chatRoom = chatRoomRepository
                .findByStudentIdAndTutorId(event.getSenderId(), event.getReceiverId())
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setStudentId(event.getSenderId());
                    newRoom.setTutorId(event.getReceiverId());
                    return chatRoomRepository.save(newRoom);
                });

        // 🔹 Tạo và lưu message
        ChatMessage message = new ChatMessage();
        message.setSenderId(event.getSenderId());
        message.setContent(event.getContent());
        message.setChatRoom(chatRoom);
        message.setTimestamp(event.getTimestamp());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 🔹 Gửi realtime cho frontend qua WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), savedMessage);

        log.info("✅ Tin nhắn đã lưu & gửi realtime: {}", savedMessage.getContent());
    }
}
