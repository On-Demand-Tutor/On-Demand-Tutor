package com.example.student_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorResponseForSearch {
    private String username;
    private Long userId;
    private String skills;
    private Double rating;
    private String teachingGrades;
    private Double price;
    private String availableTime;
}
