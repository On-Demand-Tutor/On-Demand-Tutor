package com.example.book_service.controller;


import com.example.book_service.dto.request.BookingRequestDTO;
import com.example.book_service.dto.response.BookingResponseDTO;
import com.example.book_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create/{tutorUserId}")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @PathVariable Long tutorUserId,
            @RequestBody BookingRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(bookingService.createBooking(tutorUserId, request, jwt));
    }


    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }
}
