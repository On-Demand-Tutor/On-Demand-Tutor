package com.example.book_service.dto.request;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequestDTO {
    private Long tutorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
