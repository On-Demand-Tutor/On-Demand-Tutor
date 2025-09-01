package com.example.user_service.dto.request;

import com.example.user_service.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Username không được để trống")
    private String username;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu phải ít nhất 8 ký tự, gồm chữ hoa, thường, số và ký tự đặc biệt"
    )
    private String password;

    @NotNull(message = "Vai trò không được để trống")
    private UserRole role;

    private String qualifications;

    private String skills;

    private String teachingGrades;

    private Integer grade;

}
