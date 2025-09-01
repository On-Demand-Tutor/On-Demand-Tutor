package com.example.user_service.service;


import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserLoginRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.*;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.event.StudentCreatedEvent;
import com.example.user_service.event.TutorCreatedEvent;
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
                    "http://tutor-service:8081/api/tutors/" +userId,
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
                    "http://tutor-service:8081/api/tutors/user/" + user.getId(),
                    TutorResponse.class
            );
            if (tutor != null) {
                return TutorResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .skills(tutor.getSkills())
                        .qualifications(tutor.getQualifications())
                        .teachingGrades(tutor.getTeachingGrades())
                        .rating(tutor.getRating())
                        .isVerified(tutor.isVerified())
                        .price(tutor.getPrice())
                        .availableTime(tutor.getAvailableTime())
                        .description(tutor.getDescription())
                        .promoFile(tutor.getPromoFile())
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


}
