package com.example.student_service.controller;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.event.ChatMessageEvent;
import com.example.student_service.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/search-tutor")
    public SearchTutorResponse searchTutor(@RequestBody SearchTutorRequest request) throws Exception {
        SearchTutorResponse response = studentService.searchTutorByNameOrSkill(request);

        System.out.println("✅ Đã gửi request search tutor ở student controller với từ khóa: " + request.getKeyword());
        System.out.println("👉 Kết quả search: " + response);

        return response;
    }

    @PostMapping("/chat/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessageEvent chatMessageEvent){

        studentService.sendChatMessage(chatMessageEvent);

        return ResponseEntity.ok("Message from student sent successfully");
    }


}
