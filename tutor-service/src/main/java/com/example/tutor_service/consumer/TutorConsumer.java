package com.example.tutor_service.consumer;

import com.example.tutor_service.dto.request.SearchTutorRequest;
import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.TutorCreatedEvent;
import com.example.tutor_service.event.TutorUpdatedEvent;
import com.example.tutor_service.event.TutorDeletedEvent;
import com.example.tutor_service.event.TutorRatingEvent;
import com.example.tutor_service.entity.TutorRating;
import com.example.tutor_service.repository.TutorRepository;
import com.example.tutor_service.repository.TutorRatingRepository;
import com.example.tutor_service.service.TutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class TutorConsumer {
    private final TutorRepository tutorRepository;
    private final TutorService tutorService;
    private final TutorRatingRepository tutorRatingRepository;


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
        if (event.getPrice() != null) {
            tutor.setPrice(event.getPrice());
        }
        if (event.getTeachingGrades() != null) {
            tutor.setTeachingGrades(event.getTeachingGrades());
        }
        tutorRepository.save(tutor);
    }

    @KafkaListener(topics = "search-tutor", groupId = "tutor-service-group", containerFactory = "kafkaListenerContainerFactoryForSearchTutor")
    public void handleSearchTutor(SearchTutorRequest request) {
        System.out.println("Nhận event từ student-service: " + request);

        tutorService.searchAndSendTutors(
                request.getRequestId(),
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        System.out.println("Đã xử lý và gửi response về student-service với requestId=" + request.getRequestId());
    }

    @KafkaListener(topics = "tutor-deleted", groupId = "tutor-service-group", containerFactory = "kafkaListenerContainerFactoryForDeleteTutor")
    public void consumeTutorDeleted(TutorDeletedEvent event) {
        System.out.println("Tutor Nhận được event delete từ Kafka rồi nhé ok ok ++++>>>: " + event);
        tutorService.deleteTutorByUserId(event.getUserId());
    }

    @KafkaListener(topics = "tutor-rating", groupId = "tutor-service-group",containerFactory = "kafkaListenerContainerFactoryForRateTutor")
    public void consume(TutorRatingEvent event) {
        log.info("Received rating event from student : {}", event);

        Tutor tutor = tutorRepository.findById(event.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        if (tutorRatingRepository.existsByTutorIdAndStudentId(event.getTutorId(), event.getStudentId())) {
            log.warn("Student {} already rated tutor {}", event.getStudentId(), event.getTutorId());
            return;
        }

        TutorRating rating = TutorRating.builder()
                .tutor(tutor)
                .studentId(event.getStudentId())
                .rating(event.getRating())
                .comment(event.getComment())
                .build();

        tutorRatingRepository.save(rating);
        log.info("Saved rating for tutor {}", tutor.getId());
    }
}