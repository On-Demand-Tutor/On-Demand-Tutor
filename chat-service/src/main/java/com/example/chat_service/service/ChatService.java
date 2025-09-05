package com.example.chat_service.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RestTemplate restTemplate;

    public boolean verifyStudent(Long userId) {
        String url = "http://student-service:8080/api/students/verify/" + userId;
        Boolean result = restTemplate.getForObject(url, Boolean.class);
        System.out.println("Đã gọi sang student để check xem id tồn tại không ");
        return result != null && result;
    }

    public boolean verifyTutor(Long userId) {
        String url = "http://tutor-service:8080/api/tutors/verify/" + userId;
        Boolean result = restTemplate.getForObject(url, Boolean.class);
        System.out.println("Đã gọi sang tutor để check xem id tồn tại không ");
        return result != null && result;
    }

}
