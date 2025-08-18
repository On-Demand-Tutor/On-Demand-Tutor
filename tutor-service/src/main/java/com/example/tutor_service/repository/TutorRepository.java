package com.example.tutor_service.repository;

import com.example.tutor_service.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor,Long> {

}
