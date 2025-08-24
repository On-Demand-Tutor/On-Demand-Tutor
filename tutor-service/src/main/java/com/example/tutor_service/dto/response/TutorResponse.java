package com.example.tutor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorResponse {
    private Long userId;
    private String skills;
    private Double rating;
    private String teachingGrades;
}
