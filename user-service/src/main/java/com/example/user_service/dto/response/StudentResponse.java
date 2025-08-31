package com.example.user_service.dto.response;

import lombok.Data;

@Data
public class StudentResponse {
    private Long id;
    private Long userId;
    private Integer grade;
}
