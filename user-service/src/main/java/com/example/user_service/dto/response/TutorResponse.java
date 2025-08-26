package com.example.user_service.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TutorResponse {
    private String username;
    private Long userId;
    private String skills;
    private Double rating;
    private String teachingGrades;
}
