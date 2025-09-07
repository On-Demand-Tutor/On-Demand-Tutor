package com.example.tutor_service.event;

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
    private Double price;
    private String teachingGrades;
}