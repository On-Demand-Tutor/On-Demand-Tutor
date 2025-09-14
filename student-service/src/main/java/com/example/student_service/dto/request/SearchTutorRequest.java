package com.example.student_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchTutorRequest {
    private String requestId;
    private String keyword;
    private int page;
    private int size;

}
