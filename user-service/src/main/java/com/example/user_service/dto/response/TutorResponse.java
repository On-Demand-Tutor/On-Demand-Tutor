package com.example.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorResponse {
    private Long id;
    private String username;
    private String email;
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

