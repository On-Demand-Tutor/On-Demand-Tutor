package com.example.student_service.repository;

import com.example.student_service.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findById(Long id);
}
