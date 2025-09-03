package com.example.user_service.controller;

import com.example.user_service.dto.request.ApiResponse;
import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserLoginRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.UserDetailResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.dto.response.UserSummaryResponse;
import com.example.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@Tag(name = "Authentication", description = "API quản lý crud user")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;


    @Operation(summary = "Đăng Kí", description = "Đăng kí user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật", description = "Cập nhật thông tin user")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id,request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/getAllUser")
    public ApiResponse<List<UserSummaryResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page) {

        Page<UserSummaryResponse> users = userService.getUsers(page);

        return ApiResponse.<List<UserSummaryResponse>>builder()
                .result(users.getContent())
                .build();
    }


    @GetMapping("/getUser/{userId}")
    public ApiResponse<Object> getUser(@PathVariable Long userId) {
        return ApiResponse.<Object>builder()
                .result(userService.getUserDetail(userId))
                .build();
    }

}
