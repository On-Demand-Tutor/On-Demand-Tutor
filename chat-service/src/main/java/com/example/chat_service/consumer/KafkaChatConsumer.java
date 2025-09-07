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

        boolean senderOk = "STUDENT".equals(event.getSenderRole())
                ? chatService.verifyStudent(event.getSenderId())
                : chatService.verifyTutor(event.getSenderId());

        boolean receiverOk = "STUDENT".equals(event.getSenderRole())
                ? chatService.verifyTutor(event.getReceiverId())
                : chatService.verifyStudent(event.getReceiverId());

        if (!(senderOk && receiverOk)) {
            log.warn("Xác thực thất bại: senderOk={}, receiverOk={}", senderOk, receiverOk);
            return;
        }

        // Xác định studentId & tutorId theo role nh okokk
        Long studentId, tutorId;
        if ("STUDENT".equals(event.getSenderRole())) {
            studentId = event.getSenderId();
            tutorId = event.getReceiverId();
        } else {
            tutorId = event.getSenderId();
            studentId = event.getReceiverId();
        }

        // 🔹 Tìm hoặc tạo ChatRoom
        ChatRoom chatRoom = chatRoomRepository
                .findByStudentIdAndTutorId(studentId, tutorId)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setStudentId(studentId);
                    newRoom.setTutorId(tutorId);
                    return chatRoomRepository.save(newRoom);
                });

        // Tạo và lưu message
        ChatMessage message = new ChatMessage();
        message.setSenderId(event.getSenderId());
        message.setContent(event.getContent());
        message.setChatRoom(chatRoom);
        message.setTimestamp(event.getTimestamp());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        //Gửi realtime cho frontend
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), savedMessage);

        log.info("Tin nhắn đã lưu & gửi realtime: {}", savedMessage.getContent());
    }

}
