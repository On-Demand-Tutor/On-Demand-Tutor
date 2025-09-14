package com.example.user_service.service;


import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserLoginRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.*;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.event.StudentCreatedEvent;
import com.example.user_service.event.StudentUpdatedEvent;
import com.example.user_service.event.TutorCreatedEvent;
import com.example.user_service.event.TutorUpdatedEvent;
import com.example.user_service.event.TutorDeletedEvent;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate;

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
                    request.getTeachingGrades(),
                    request.getPrice(),
                    request.getDescription()
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
                    request.getTeachingGrades(),
                    request.getPrice(),
                    request.getDescription()
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

    public void login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
    }

    public Page<UserSummaryResponse> getUsers(int page) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build()
        );
    }


    public Object getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.STUDENT) {
            StudentResponse student = restTemplate.getForObject(
                    "http://student-service:8080/api/students/user/" + user.getId(),
                    StudentResponse.class
            );
            if (student != null) {
                return StudentResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .grade(student.getGrade())
                        .build();
            }
        }

        if (user.getRole() == UserRole.TUTOR) {
            TutorResponse tutor = restTemplate.getForObject(
                    "http://tutor-service:8080/api/tutors/user/" + user.getId(),
                    TutorResponse.class
            );
            if (tutor != null) {
                return TutorResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .qualifications(tutor.getQualifications())
                        .skills(tutor.getSkills())
                        .teachingGrades(tutor.getTeachingGrades())
                        .price(tutor.getPrice())
                        .description(tutor.getDescription())
                        .build();
            }
        }

        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public String getUsernameByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public String getEmailByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
   
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("Deleted user {}", userId);

        // Gửi event luôn sau khi xóa
        TutorDeletedEvent event = new TutorDeletedEvent(userId);
        kafkaTemplate.send("tutor-deleted", event);
        log.info("Published tutor-deleted event for userId={}", userId);
    }
}
