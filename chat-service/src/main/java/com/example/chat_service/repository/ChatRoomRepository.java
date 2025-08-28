package com.example.chat_service.repository;

import com.example.chat_service.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByStudentIdAndTutorId(String studentId, String tutorId);
}
