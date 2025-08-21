package com.example.user_service.event;

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
    private String teachingGrades;
}
