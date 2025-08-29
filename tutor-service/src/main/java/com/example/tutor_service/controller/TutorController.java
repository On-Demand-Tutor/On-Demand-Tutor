package com.example.tutor_service.controller;

import com.example.tutor_service.dto.request.MessageRequest;
import com.example.tutor_service.event.ChatMessageEvent;
import com.example.tutor_service.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

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
