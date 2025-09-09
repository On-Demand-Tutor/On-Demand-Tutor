package com.example.book_service.mapper;

import com.example.book_service.dto.request.BookingRequestDTO;
import com.example.book_service.dto.response.BookingResponseDTO;
import com.example.book_service.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toBooking(BookingRequestDTO bookingRequestDTO);

    BookingResponseDTO toResponseDTO(Booking booking);
}
