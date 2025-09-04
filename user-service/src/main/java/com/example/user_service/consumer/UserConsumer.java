package com.example.user_service.consumer;

import com.example.user_service.dto.response.SearchTutorResponse;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConsumer {

    private final UserService userService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "search-user-for-tutors", groupId = "user-service-group", containerFactory = "kafkaListenerContainerFactoryForSearchTutorResponse")
    public void enrichTutorWithUsername(SearchTutorResponse response) {
        response.getTutors().forEach(tutor -> {
            String username = userService.getUsernameByUserId(tutor.getUserId());
            tutor.setUsername(username);
        });

        kafkaTemplate.send("search-tutor-final-response", response);
        System.out.println("Đã enrich username và gửi về student-service: " + response);
    }

}
