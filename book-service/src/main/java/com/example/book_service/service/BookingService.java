package com.example.book_service.service;


import com.example.book_service.dto.request.BookingRequestDTO;
import com.example.book_service.dto.response.BookingResponseDTO;
import com.example.book_service.dto.response.StudentResponse;
import com.example.book_service.dto.response.TutorResponse;
import com.example.book_service.entity.Booking;
import com.example.book_service.enums.BookingStatus;
import com.example.book_service.event.BookingEvent;
import com.example.book_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import com.example.book_service.mapper.BookingMapper;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingResponseDTO createBooking(Long tutorUserId, BookingRequestDTO request, Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        Long studentId=getStudentIdByUserId(userId);
        TutorResponse tutor = getTutorIdByUserId(tutorUserId);
        String email = getEmailByUserId(userId);
        log.info("đã gọi sang user-sservice");

        Booking booking = bookingMapper.toBooking(request);
        booking.setStudentId(studentId);
        booking.setTutorId(tutor.getId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setEmail(email);

        Booking saved = bookingRepository.save(booking);
        BookingEvent event = BookingEvent.builder()
                .studentId(saved.getStudentId())
                .tutorId(saved.getTutorId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .skills(tutor.getSkills())
                .email(email)
                .build();

        kafkaTemplate.send("booking-events", event);

        log.info("sent to notifications roi nhe ok ok ok ok=========>>>>>>>");

        return bookingMapper.toResponseDTO(saved);
    }

    public String getEmailByUserId(Long userId) {
        String url = "http://user-service:8080/api/users/email/" + userId;
        return restTemplate.getForObject(url, String.class);
    }

    public Long getStudentIdByUserId(Long userId) {
        String url = "http://student-service:8080/api/students/user/" + userId;
        StudentResponse student = restTemplate.getForObject(url, StudentResponse.class);
        if (student == null) {
            throw new RuntimeException("Student not found with userId=" + userId);
        }
        return student.getId();
    }

    public TutorResponse getTutorIdByUserId(Long tutorId) {
        String url = "http://tutor-service:8080/api/tutors/user/" + tutorId;
        TutorResponse tutor = restTemplate.getForObject(url, TutorResponse.class);
        if (tutor == null) {
            throw new RuntimeException("Tutor not found with id=" + tutorId);
        }
        return tutor;
    }

    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BookingResponseDTO getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
