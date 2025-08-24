package com.example.tutor_service.consumer;


import com.example.tutor_service.dto.request.SearchTutorRequest;
import com.example.tutor_service.dto.response.SearchTutorResponse;
import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.TutorCreatedEvent;
import com.example.tutor_service.event.TutorUpdatedEvent;
import com.example.tutor_service.repository.TutorRepository;
import com.example.tutor_service.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
@RequiredArgsConstructor
public class TutorConsumer {

    private final TutorService tutorService;

    private final TutorRepository tutorRepository;

    @KafkaListener(topics = "tutor-created", groupId = "tutor-service-group",containerFactory = "kafkaListenerContainerFactoryForCreateTutor")
    public void consumeTutorCreated(TutorCreatedEvent event) {
        System.out.println("Tutor Nhận được event từ Kafka rồi nhé ok ok ++++>>>: " + event);
        Tutor tutor = Tutor.builder()
                .userId(event.getUserId())
                .qualifications(event.getQualifications())
                .skills(event.getSkills())
                .teachingGrades(event.getTeachingGrades())
                .build();
        tutorRepository.save(tutor);
    }

    @KafkaListener(topics = "tutor-updated", groupId = "tutor-service-group", containerFactory = "kafkaListenerContainerFactoryForUpdateTutor")
    public void consumeTutorUpdated(TutorUpdatedEvent event) {
        System.out.println("Tutor Nhận được event update từ Kafka rồi nhé ok ok ++++>>>: " + event);

        Tutor tutor = tutorRepository.findByUserId(event.getUserId())
                .orElseThrow(() -> new RuntimeException("Tutor not found with userId: " + event.getUserId()));
        if (event.getQualifications() != null) {
            tutor.setQualifications(event.getQualifications());
        }
        if (event.getSkills() != null) {
            tutor.setSkills(event.getSkills());
        }
        if (event.getTeachingGrades() != null) {
            tutor.setTeachingGrades(event.getTeachingGrades());
        }
        tutorRepository.save(tutor);
    }

    @KafkaListener(topics = "search-tutor", groupId = "tutor-service-service", containerFactory = "kafkaListenerContainerFactoryForSearchTutor")
    public void handleSearchTutor(SearchTutorRequest request) {
        System.out.println("Nhận event từ student-service: " + request);

        int page=request.getPage();
        int size=request.getSize();

        SearchTutorResponse result = tutorService.searchTutorsWithPagination(request.getKeyword(), page, size);

        tutorService.sendSearchResponse(request.getRequestId(), result.getTutors(), result.getTotalElements());
    }
}
