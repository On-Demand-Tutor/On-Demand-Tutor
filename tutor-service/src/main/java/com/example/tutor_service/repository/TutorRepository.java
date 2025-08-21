// package com.example.tutor_service.repository;

// import com.example.tutor_service.entity.Tutor;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;

// public interface TutorRepository extends JpaRepository<Tutor,Long> {

//     Optional<Tutor> findByUserId(Long userId);
// }

package com.example.tutor_service.repository;

import com.example.tutor_service.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByUserId(Long userId);
}
