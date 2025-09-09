package com.example.student_service.repository;

import com.example.student_service.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findById(Long id);
}