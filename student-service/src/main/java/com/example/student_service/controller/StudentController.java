package com.example.student_service.controller;

import com.example.student_service.dto.request.SearchTutorRequest;
import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.entity.Student;
import com.example.student_service.repository.StudentRepository;
import com.example.student_service.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;

    private final StudentService studentService;

    @PostMapping
    public Student createStudent(@RequestBody Map<String, Object> studentData) {
        Student student = Student.builder()
                .userId(Long.valueOf(studentData.get("userId").toString()))
                .grade(studentData.get("grade") != null ?
                        Integer.valueOf(studentData.get("grade").toString()) : 1)
                .build();

        return studentRepository.save(student);
    }

    @PutMapping("/{userId}")
    public Student updateStudent(@PathVariable Long userId, @RequestBody Map<String, Object> studentData) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (studentData.get("grade") != null) {
            student.setGrade(Integer.valueOf(studentData.get("grade").toString()));
        }

        return studentRepository.save(student);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Student> getStudentByUserId(@PathVariable Long userId) {
        Student student = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(student);
    }

    @PostMapping("/search-tutor")
    public SearchTutorResponse searchTutor(@RequestBody SearchTutorRequest request) throws Exception {
        SearchTutorResponse response = studentService.searchTutorBySkill(request);

        System.out.println("✅ Đã gửi request search tutor ở student controller với từ khóa: " + request.getKeyword());
        System.out.println("👉 Kết quả search: " + response);

        return response;
    }

    @PostMapping("/chat/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessageEvent chatMessageEvent){

        studentService.sendChatMessage(chatMessageEvent);

        return ResponseEntity.ok("Message from student sent successfully");
    }

    @GetMapping("/verify/{userId}")
    public ResponseEntity<Boolean> verifyStudent(@PathVariable Long userId) {
        return ResponseEntity.ok(studentService.verifyStudent(userId));
    }


}

