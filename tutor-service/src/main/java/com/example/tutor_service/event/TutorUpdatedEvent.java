package com.example.tutor_service.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorUpdatedEvent {
    private Long userId;
    private String qualifications;
    private String skills;
    private Double price;
    private String teachingGrades;
}
