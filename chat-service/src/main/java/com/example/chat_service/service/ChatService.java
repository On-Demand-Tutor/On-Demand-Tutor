package com.example.chat_service.service;


import com.example.chat_service.dto.response.StudentResponse;
import com.example.chat_service.dto.response.TutorResponse;
import com.example.chat_service.entity.ChatMessage;
import com.example.chat_service.entity.ChatRoom;
import com.example.chat_service.repository.ChatMessageRepository;
import com.example.chat_service.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RestTemplate restTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

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

    public ChatRoom getChatRoom(Long studentId, Long tutorId) {
        return chatRoomRepository.findByStudentIdAndTutorId(studentId, tutorId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    public List<ChatMessage> getMessages(Long studentId, Long tutorId) {
        ChatRoom chatRoom = getChatRoom(studentId, tutorId);
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoom.getId());
    }

    public Long getStudentIdByUserId(Long userId) {
        String url = "http://student-service:8080/api/students/user/" + userId;
        StudentResponse student = restTemplate.getForObject(url, StudentResponse.class);
        if (student == null) {
            throw new RuntimeException("Student not found with userId=" + userId);
        }
        return student.getId();
    }

    public Long getTutorIdByUserId(Long userId) {
        String url = "http://tutor-service:8080/api/tutors/user/" + userId;
        TutorResponse tutor = restTemplate.getForObject(url, TutorResponse.class);
        if (tutor == null) {
            throw new RuntimeException("Tutor not found with userId=" + userId);
        }
        return tutor.getId();
    }

    public List<ChatMessage> getMessagesByUser(Long currentUserId, String role, Long targetUserId) {
        System.out.println("currentUserId=" + currentUserId + ", role=" + role + ", targetUserId=" + targetUserId);

        Long studentId;
        Long tutorId;

        if ("ROLE_STUDENT".equals(role) || "STUDENT".equals(role)) {
            studentId = getStudentIdByUserId(currentUserId);
            tutorId = getTutorIdByUserId(targetUserId);
            System.out.println("Resolved studentId=" + studentId + ", tutorId=" + tutorId);
        } else if ("ROLE_TUTOR".equals(role) || "TUTOR".equals(role)) {
            tutorId = getTutorIdByUserId(currentUserId);
            studentId = getStudentIdByUserId(targetUserId);
            System.out.println("Resolved tutorId=" + tutorId + ", studentId=" + studentId);
        } else {
            throw new RuntimeException("Invalid role: " + role);
        }

        return getMessages(studentId, tutorId);
    }


}
