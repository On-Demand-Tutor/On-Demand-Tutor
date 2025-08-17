package com.example.tutor_service.repository;

import com.example.tutor_service.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor,Long> {

    Optional<Tutor> findByUserId(Long userId);
}
