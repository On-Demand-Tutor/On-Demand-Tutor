package com.example.book_service.repository;

import com.example.book_service.entity.Booking;
import com.example.book_service.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    boolean existsByStudentIdAndTutorIdAndStatus(Long studentId, Long tutorId, BookingStatus status);
}
