package com.example.tutor_service.controller;

import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.event.ChatMessageEvent;
import com.example.tutor_service.repository.TutorRepository;
import com.example.tutor_service.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorRepository tutorRepository;
    private final TutorService tutorService;

    // Tạo mới Tutor
    @PostMapping
    public Tutor createTutor(@RequestBody Map<String, Object> tutorData) {
        Tutor tutor = Tutor.builder()
                .userId(Long.valueOf(tutorData.get("userId").toString()))
                .qualifications((String) tutorData.get("qualifications"))
                .skills((String) tutorData.get("skills"))
                .price(tutorData.get("price") != null ?
                        Double.valueOf(tutorData.get("price").toString()) : 0.0)
                .teachingGrades((String) tutorData.get("teachingGrades"))
                .build();

        return tutorRepository.save(tutor);
    }

    // Cập nhật Tutor theo userId
    @PutMapping("/{userId}")
    public Tutor updateTutor(@PathVariable Long userId, @RequestBody Map<String, Object> tutorData) {
        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        if (tutorData.get("qualifications") != null) {
            tutor.setQualifications((String) tutorData.get("qualifications"));
        }
        if (tutorData.get("skills") != null) {
            tutor.setSkills((String) tutorData.get("skills"));
        }
        if (tutorData.get("price") != null) {
            tutor.setPrice(Double.valueOf(tutorData.get("price").toString()));
        }
        if (tutorData.get("teachingGrades") != null) {
            tutor.setTeachingGrades((String) tutorData.get("teachingGrades"));
        }

        return tutorRepository.save(tutor);
    }

    // Lấy thông tin Tutor theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<Tutor> getTutorByUserId(@PathVariable Long userId) {
        Tutor tutor = tutorService.getTutorByUserId(userId);
        return ResponseEntity.ok(tutor);
    }

    @PostMapping("/chat/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessageEvent chatMessageEvent) {

        tutorService.sendChatMessage(chatMessageEvent);
        System.out.println("Message from tutor sent successfully  terminal");

        return ResponseEntity.ok("Message from tutor sent successfully postman");
    }

    @GetMapping("/verify/{userId}")
    public ResponseEntity<Boolean> verifyTutor(@PathVariable Long userId) {
        return ResponseEntity.ok(tutorService.verifyTutor(userId));
    }
}
