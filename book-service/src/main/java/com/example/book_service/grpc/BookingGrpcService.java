package com.example.book_service.grpc;

import com.example.book_service.repository.BookingRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class BookingGrpcService extends BookingServiceGrpc.BookingServiceImplBase {

    private final BookingRepository bookingRepository;

    @Override
    public void checkBooking(CheckBookingRequest request,
                             StreamObserver<CheckBookingResponse> responseObserver) {

        boolean exists = bookingRepository
                .findByStudentIdAndTutorId(request.getStudentId(), request.getTutorId())
                .isPresent();

        CheckBookingResponse response = CheckBookingResponse.newBuilder()
                .setExists(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}