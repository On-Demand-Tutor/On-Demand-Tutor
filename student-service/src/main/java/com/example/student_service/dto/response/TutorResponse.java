package com.example.student_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorResponse {
    private String username;
    private Long userId;
    private String skills;
    private Double rating;
    private String teachingGrades;
}
