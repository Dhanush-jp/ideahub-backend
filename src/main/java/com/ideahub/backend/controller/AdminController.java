package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.admin.AdminQueryRequest;
import com.ideahub.backend.dto.admin.AdminLoginRequest;
import com.ideahub.backend.dto.admin.AdminQueryResponse;
import com.ideahub.backend.dto.auth.AuthResponse;
import com.ideahub.backend.exception.BadRequestException;
import com.ideahub.backend.service.AdminQueryService;
import com.ideahub.backend.repository.UserRepository;
import com.ideahub.backend.security.JwtTokenProvider;
import com.ideahub.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminQueryService adminQueryService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.admin.username:admin123}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AdminLoginRequest req) {
        if (req == null
                || req.getUsername() == null
                || req.getPassword() == null
                || !req.getUsername().equalsIgnoreCase(adminUsername)
                || !req.getPassword().equals(adminPassword)) {
            throw new BadCredentialsException("Invalid admin credentials");
        }

        UserPrincipal principal = userRepository.findByUsernameIgnoreCase(adminUsername)
                .map(UserPrincipal::new)
                .orElse(null);
        if (principal == null) {
            throw new BadCredentialsException("Admin account is not available");
        }

        AuthResponse authResponse = AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(principal))
                .userId(principal.getId())
                .username(adminUsername)
                .email(principal.getEmail())
                .role("ROLE_ADMIN")
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AdminQueryResponse>> query(@RequestBody AdminQueryRequest request) {
        String input = request.getQuestion();
        if (input == null || input.isBlank()) {
            input = request.getQuery();
        }
        if (input == null || input.isBlank()) {
            throw new BadRequestException("Query is required");
        }
        return ResponseEntity.ok(ApiResponse.success(adminQueryService.runNaturalLanguageQuery(input)));
    }
}
