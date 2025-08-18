package com.example.tutor_service.consumer;


import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.TutorCreatedEvent;
import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorConsumer {

    private final TutorRepository tutorRepository;

    @KafkaListener(topics = "tutor-created", groupId = "tutor-service-group")
    public void consumeTutorCreated(TutorCreatedEvent event) {
        Tutor tutor = Tutor.builder()
                .userId(event.getUserId())
                .qualifications(event.getQualifications())
                .skills(event.getSkills())
                .build();
        tutorRepository.save(tutor);
    }
}
