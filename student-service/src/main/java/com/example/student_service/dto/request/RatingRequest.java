package com.example.student_service.dto.request;

import lombok.Data;

@Data
public class RatingRequest {
    private Double rating;
    private String comment;
}
