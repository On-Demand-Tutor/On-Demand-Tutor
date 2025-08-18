package com.example.user_service.service;


import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserLoginRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.event.StudentCreatedEvent;
import com.example.user_service.event.TutorCreatedEvent;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        // 👇 Publish event sang 2 bên để dùng ROLE
        if (savedUser.getRole() == UserRole.STUDENT) {
            StudentCreatedEvent event = new StudentCreatedEvent(
                    savedUser.getId(),
                    request.getGrade()
            );
            kafkaTemplate.send("student-created", event);
        }

        if (savedUser.getRole() == UserRole.TUTOR) {
            TutorCreatedEvent event = new TutorCreatedEvent(
                    savedUser.getId(),
                    request.getQualifications(),
                    request.getSkills(),
                    request.getTeachingGrades()
            );
            kafkaTemplate.send("tutor-created", event);
        }

        return UserResponse.builder()
                .id(String.valueOf(savedUser.getId()))
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .build();
    }

    public UserResponse updateUser(Long userId,UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        if (user.getRole() == UserRole.STUDENT) {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("userId", user.getId());
            studentData.put("grade", request.getGrade());

            restTemplate.put(
                    "http://student-service:8080/api/students/" + userId,
                    studentData
            );
        }

        if (user.getRole() == UserRole.TUTOR) {
            Map<String, Object> tutorData = new HashMap<>();
            tutorData.put("userId", user.getId());
            tutorData.put("qualifications", request.getQualifications());
            tutorData.put("skills", request.getSkills());
            tutorData.put("teachingGrades", request.getTeachingGrades());

            restTemplate.put(
                    "http://tutor-service:8080/api/tutors/" +userId,
                    tutorData
            );
        }

        return UserResponse.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }



    public void login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
    }

}
