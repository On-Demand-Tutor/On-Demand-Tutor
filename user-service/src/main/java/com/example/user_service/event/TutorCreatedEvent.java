package com.example.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorCreatedEvent {
    private Long userId;
    private String qualifications;
    private String skills;
    private String teachingGrades;
    private Double price;
}
