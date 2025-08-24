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

import java.util.List;


@Service
@RequiredArgsConstructor
public class TutorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TutorRepository tutorRepository;

    public SearchTutorResponse searchTutorsWithPagination(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tutor> tutorPage = tutorRepository.findBySkillsContainingOrQualificationsContaining(keyword, keyword, pageable);

        List<Tutor> tutors = tutorPage.getContent();

        return new SearchTutorResponse(tutors, tutorPage.getTotalElements());
    }

    public void sendSearchResponse(String requestId, List<Tutor> tutors, long totalElements) {
        SearchTutorResponse response = new SearchTutorResponse(tutors, totalElements);
        kafkaTemplate.send("search-tutor-response", response);
        System.out.println("Đã gửi response sang student-service: " + response);
    }
}
