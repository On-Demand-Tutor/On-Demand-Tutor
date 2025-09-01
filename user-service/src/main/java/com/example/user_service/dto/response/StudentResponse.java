package com.example.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentResponse {
    private Long id;
    private String username;
    private String email;
    private Integer grade;
}
