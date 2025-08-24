package com.example.student_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchTutorRequest {
    private String keyword;
    private int page;
    private int size;
}
