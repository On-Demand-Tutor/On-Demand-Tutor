package com.example.user_service.event;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TutorDeletedEvent {
    private Long userId;
}