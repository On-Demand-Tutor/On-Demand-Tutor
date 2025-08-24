package com.example.tutor_service.repository;

import com.example.tutor_service.dto.response.TutorResponse;
import com.example.tutor_service.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor,Long> {

    Optional<Tutor> findByUserId(Long userId);

    Page<TutorResponse> findBySkillsContainingOrQualificationsContaining(String keyword, String keyword1, Pageable pageable);
}
