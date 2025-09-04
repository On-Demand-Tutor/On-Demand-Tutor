package com.example.student_service.service;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.entity.Student;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service

public class StudentService {

    private final StudentRepository studentRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, CompletableFuture<SearchTutorResponse>> pendingRequests = new ConcurrentHashMap<>();

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

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
