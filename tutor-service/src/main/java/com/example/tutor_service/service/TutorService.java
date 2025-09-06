package com.example.tutor_service.service;

import com.example.tutor_service.dto.response.SearchTutorResponse;
import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorService {
    private final TutorRepository tutorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Tutor getTutorByUserId(Long userId) {
        return tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
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
}
