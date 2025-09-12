package com.example.tutor_service.repository;

import com.example.tutor_service.entity.TutorRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRatingRepository extends JpaRepository<TutorRating,Long> {
    boolean existsByTutorIdAndStudentId(Long tutorId, Long studentId);
}