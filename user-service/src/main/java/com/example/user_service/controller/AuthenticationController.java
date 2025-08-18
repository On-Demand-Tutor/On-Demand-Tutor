package com.example.user_service.controller;

import com.example.user_service.dto.request.ApiResponse;
import com.example.user_service.dto.request.LogoutRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.UserLoginRequest;
import com.example.user_service.dto.response.AuthenticationResponse;
import com.example.user_service.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody UserLoginRequest request) {
        var result = authenticationService.Login(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Login successful")
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws Exception {
        var result = authenticationService.refeshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Token refreshed successfully")
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws Exception {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout successful")
                .build();
    }
}
