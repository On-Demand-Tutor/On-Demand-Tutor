package com.example.student_service.service;

import com.example.student_service.entity.Student;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service

public class StudentService {

    private final StudentRepository studentRepository;

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

}
