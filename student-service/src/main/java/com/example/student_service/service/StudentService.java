package com.example.student_service.service;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StudentService {

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
}

