package com.example.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchTutorResponse {
    private String requestId;
    private List<TutorResponse> tutors;
    private Long totalElements;
}
