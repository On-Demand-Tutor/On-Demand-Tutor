package com.example.user_service.dto.response;

import lombok.Data;

@Data
public class TutorResponse {
    private Long id;
    private Long userId;
    private String skills;
    private String qualifications;
    private String teachingGrades;
    private Double rating;
    private boolean isVerified;
    private Double price;
    private String availableTime;
    private String description;
    private String promoFile;
}
