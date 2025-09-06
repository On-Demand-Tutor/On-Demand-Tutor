package com.example.tutor_service.consumer;


import com.example.tutor_service.dto.request.SearchTutorRequest;
import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.TutorCreatedEvent;
import com.example.tutor_service.repository.TutorRepository;
import com.example.tutor_service.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor
public class TutorConsumer {
    private final TutorRepository tutorRepository;
    private final TutorService tutorService;


    @KafkaListener(topics = "tutor-created", groupId = "tutor-service-group",containerFactory = "kafkaListenerContainerFactory")
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

    @KafkaListener(topics = "search-tutor", groupId = "tutor-service-service", containerFactory = "kafkaListenerContainerFactoryForSearchTutor")
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
}