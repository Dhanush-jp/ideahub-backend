package com.ideahub.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String token;
    private final Long userId;
    private final String username;
    private final String email;
    private final String role;
}
