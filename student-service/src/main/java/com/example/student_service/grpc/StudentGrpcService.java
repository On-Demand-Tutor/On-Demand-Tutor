package com.example.student_service.grpc;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class StudentGrpcService extends BookingServiceGrpc.BookingServiceImplBase {


    @GrpcClient("book-service")
    private BookingServiceGrpc.BookingServiceBlockingStub bookingStub;

    public boolean checkBooking(Long studentId, Long tutorId) {
        CheckBookingRequest request = CheckBookingRequest.newBuilder()
                .setStudentId(studentId)
                .setTutorId(tutorId)
                .build();

        CheckBookingResponse response = bookingStub.checkBooking(request);
        return response.getExists();
    }
}