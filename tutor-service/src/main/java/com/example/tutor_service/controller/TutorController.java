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

    private final TutorService tutorService;

    @PostMapping("/chat/send-message")
    public ResponseEntity<String> sendMessage(
            @RequestBody String tutorId,
            @RequestBody String studentId,
            @RequestBody String messageContent) {

        ChatMessageEvent event = new ChatMessageEvent();
        event.setSenderId(tutorId);
        event.setSenderType("TUTOR");
        event.setSenderName("Tutor Name");
        event.setReceiverId(studentId);
        event.setReceiverType("STUDENT");
        event.setContent(messageContent);

        tutorService.sendChatMessage(event);
        System.out.println("Message sent successfully");

        return ResponseEntity.ok("Message from tutor sent successfully");
    }


}
