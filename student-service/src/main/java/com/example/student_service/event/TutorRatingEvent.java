package com.example.student_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TutorRatingEvent {
    private Long tutorId;
    private Long studentId;
    private Double rating;
    private String comment;
}
