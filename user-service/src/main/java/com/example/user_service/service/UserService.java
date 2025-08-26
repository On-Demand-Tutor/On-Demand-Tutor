package com.example.user_service.service;


import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.event.StudentCreatedEvent;
import com.example.user_service.event.StudentUpdatedEvent;
import com.example.user_service.event.TutorCreatedEvent;
import com.example.user_service.event.TutorUpdatedEvent;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse register(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        //Publish event sang 2 bên để dùng ROLE rồi check thôi kkk
        if (savedUser.getRole() == UserRole.STUDENT) {
            StudentCreatedEvent event = new StudentCreatedEvent(
                    savedUser.getId(),
                    request.getGrade()
            );
            kafkaTemplate.send("student-created", event);
            System.out.println("Đã gửi Kafka event tới Student=========================================================: " + event);
        }

        if (savedUser.getRole() == UserRole.TUTOR) {
            TutorCreatedEvent event = new TutorCreatedEvent(
                    savedUser.getId(),
                    request.getQualifications(),
                    request.getSkills(),
                    request.getTeachingGrades()
            );
            kafkaTemplate.send("tutor-created", event);
            System.out.println("Đã gửi Kafka event tới Tutor:========================================================= " + event);
        }

        return UserResponse.builder()
                .id(String.valueOf(savedUser.getId()))
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .build();
    }

    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        //Publish event sang 2 bên để dùng ROLE rồi check thôi kkk
        if (updatedUser.getRole() == UserRole.STUDENT) {
            StudentUpdatedEvent event = new StudentUpdatedEvent(
                    updatedUser.getId(),
                    request.getGrade()
            );
            kafkaTemplate.send("student-updated", event);
            System.out.println("Đã gửi Kafka event update Student=========================================================: " + event);
        }

        if (updatedUser.getRole() == UserRole.TUTOR) {
            TutorUpdatedEvent event = new TutorUpdatedEvent(
                    updatedUser.getId(),
                    request.getQualifications(),
                    request.getSkills(),
                    request.getTeachingGrades()
            );
            kafkaTemplate.send("tutor-updated", event);
            System.out.println("Đã gửi Kafka event update Tutor========================================================= " + event);
        }

        return UserResponse.builder()
                .id(String.valueOf(updatedUser.getId()))
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .build();
    }

    public String getUsernameByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }


}
