package com.example.user_service.dto.response;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private boolean authenticated;
}
