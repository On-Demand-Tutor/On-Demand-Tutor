package com.example.book_service.service;


import com.example.book_service.dto.request.BookingRequestDTO;
import com.example.book_service.dto.response.BookingResponseDTO;
import com.example.book_service.dto.response.TutorResponse;
import com.example.book_service.entity.Booking;
import com.example.book_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import com.example.book_service.mapper.BookingMapper;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RestTemplate restTemplate;

    public BookingResponseDTO createBooking(Long tutorUserId, BookingRequestDTO request, Jwt jwt) {
        Long studentId = Long.parseLong(jwt.getClaim("id").toString());

        Long tutorId = getTutorIdByUserId(tutorUserId);

        Booking booking = bookingMapper.toBooking(request);
        booking.setStudentId(studentId);
        booking.setTutorId(tutorId);

        Booking saved = bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(saved);
    }

    private Long getTutorIdByUserId(Long userId) {
        String url = "http://tutor-service:8080/api/tutors/user/" + userId;
        TutorResponse tutor = restTemplate.getForObject(url, TutorResponse.class);
        if (tutor == null) {
            throw new RuntimeException("Tutor not found with userId=" + userId);
        }
        return tutor.getId();
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
