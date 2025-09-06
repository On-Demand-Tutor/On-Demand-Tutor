package com.example.tutor_service.service;

import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.ChatMessageEvent;
import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {
    private final TutorRepository tutorRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public Tutor getTutorByUserId(Long userId) {
        return tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }

    @Value("${kafka.topic.chat-messages}")
    private String chatMessagesTopic;

    public void sendChatMessage(ChatMessageEvent chatMessageEvent) {
        try {
            kafkaTemplate.send(chatMessagesTopic, chatMessageEvent);
            log.info("Sent chat message event to Kafka: {}", chatMessageEvent.getContent());

        } catch (Exception e) {
            log.error("Failed to send chat message to Kafka: {}", e.getMessage());
            throw new RuntimeException("Failed to send chat message", e);
        }
    }

    public boolean verifyTutor(Long id) {
        return tutorRepository.findById(id).isPresent();
    }

}
