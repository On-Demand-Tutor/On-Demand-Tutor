package com.example.student_service.service;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public SearchTutorResponse searchTutorByNameOrSkill(SearchTutorRequest request) {
        kafkaTemplate.send("search-tutor", request);
        System.out.println("Đã gửi request search tutor với từ khóa: " + request.getKeyword());

        SearchTutorResponse response = new SearchTutorResponse();
        return response;
    }


}
