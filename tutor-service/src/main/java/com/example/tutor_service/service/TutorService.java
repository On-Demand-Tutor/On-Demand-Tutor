package com.example.tutor_service.service;

import com.example.tutor_service.dto.response.StudentResponse;
import com.example.tutor_service.dto.response.SearchTutorResponse;
import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.ChatMessageEvent;
import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final RestTemplate restTemplate;

    private final TutorRepository tutorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Tutor getTutorByUserId(Long userId) {
        return tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }


    public Long getStudentIdByUserId(Long userId) {
        String url = "http://student-service:8080/api/students/user/" + userId;
        StudentResponse student = restTemplate.getForObject(url, StudentResponse.class);
        if (student == null) {
            throw new RuntimeException("Student not found with userId=" + userId);
        }
        return student.getId();
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

    public void searchAndSendTutors(String requestId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tutor> tutorPage = tutorRepository.findBySkillsContaining(keyword, pageable);

        SearchTutorResponse response = new SearchTutorResponse();
        response.setRequestId(requestId);
        response.setTutors(tutorPage.getContent());
        response.setTotalElements(tutorPage.getTotalElements());

        kafkaTemplate.send("search-user-for-tutors", response);
        System.out.println("Đã gửi response sang user-service để enrich username: " + response);
    }

    @Transactional
    public void deleteTutorByUserId(Long userId) {
        if (tutorRepository.existsByUserId(userId)) {
            tutorRepository.deleteByUserId(userId);
            System.out.println("Đã xóa tutor với userId=" + userId);
        }
    }
}
