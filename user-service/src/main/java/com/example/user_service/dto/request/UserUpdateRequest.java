package com.example.user_service.dto.request;


import com.example.user_service.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String username;
    private String password;

    // chỉ cho student
    private Integer grade;

    // chỉ cho tutor
    private String qualifications;
    private String skills;
    private String teachingGrades;

    private String availableTime;
    private String description;
}
