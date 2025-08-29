package com.example.student_service.service;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.event.ChatMessageEvent;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, CompletableFuture<SearchTutorResponse>> pendingRequests = new ConcurrentHashMap<>();

    public SearchTutorResponse searchTutorByNameOrSkill(SearchTutorRequest request) throws Exception {
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        CompletableFuture<SearchTutorResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        kafkaTemplate.send("search-tutor", request);
        System.out.println("Đã gửi request search tutor ở student service với từ khóa: " + request.getKeyword());

        return future.get(5, TimeUnit.SECONDS);
    }

    public void handleSearchTutorResponse(SearchTutorResponse response) {
        CompletableFuture<SearchTutorResponse> future = pendingRequests.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        }
    }


    @Value("${kafka.topic.chat-messages}")
    private String chatMessagesTopic;

    public void sendChatMessage(ChatMessageEvent chatMessageEvent) {
        try {
            kafkaTemplate.send(chatMessagesTopic,chatMessageEvent );

        } catch (Exception e) {
            log.error("Failed to send chat message to Kafka: {}", e.getMessage());
        }
    }

    public boolean verifyStudent(Long id) {
        return studentRepository.findById(id).isPresent();
    }

}

