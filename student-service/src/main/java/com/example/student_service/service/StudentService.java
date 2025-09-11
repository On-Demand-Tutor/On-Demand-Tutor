package com.example.student_service.service;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.dto.response.TutorResponse;
import com.example.student_service.entity.Student;
import com.example.student_service.event.ChatMessageEvent;
import com.example.student_service.event.TutorRatingEvent;
import com.example.student_service.grpc.StudentGrpcService;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private final StudentGrpcService studentGrpcService;

    private final RestTemplate restTemplate;

    private final StudentRepository studentRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, CompletableFuture<SearchTutorResponse>> pendingRequests = new ConcurrentHashMap<>();

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public SearchTutorResponse searchTutorBySkill(SearchTutorRequest request) throws Exception {
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

    public Long getTutorIdByUserId(Long userId) {
        String url = "http://tutor-service:8080/api/tutors/user/" + userId;
        TutorResponse tutor = restTemplate.getForObject(url, TutorResponse.class);
        if (tutor == null) {
            throw new RuntimeException("Tutor not found with userId=" + userId);
        }
        return tutor.getId();
    }

    @Value("${kafka.topic.chat-messages}")
    private String chatMessagesTopic;

    public void sendChatMessage(ChatMessageEvent chatMessageEvent) {
        try {
            kafkaTemplate.send(chatMessagesTopic,chatMessageEvent );
            log.info("Sent chat message event to Kafka: {}", chatMessageEvent.getContent());
        } catch (Exception e) {
            log.error("Failed to send chat message to Kafka: {}", e.getMessage());
        }
    }

    public boolean verifyStudent(Long id) {
        return studentRepository.findById(id).isPresent();
    }


    public void rateTutor(Long tutorUserId, Long userId, Double rating, String comment) {
        log.info("=============>>>>>>>>dang o ben phia StudentSerrvice");

        Student student = getStudentByUserId(userId);
        Long studentId = student.getId();

        Long tutorId = getTutorIdByUserId(tutorUserId);
        boolean exists = studentGrpcService.checkBooking(studentId, tutorId);

        if (!exists) {
            throw new IllegalArgumentException("You cannot rate this tutor without a booking!");
        }

        TutorRatingEvent event = new TutorRatingEvent(tutorId, studentId, rating, comment);
        kafkaTemplate.send("tutor-rating", event);
        log.info("sent event ===>>>>>tutor service=====>>>>");
    }


}
