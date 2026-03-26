package com.ideahub.backend.service;

import com.ideahub.backend.dto.auth.AuthResponse;
import com.ideahub.backend.dto.auth.LoginRequest;
import com.ideahub.backend.dto.auth.RegisterRequest;
import com.ideahub.backend.exception.ConflictException;
import com.ideahub.backend.model.Role;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.UserRepository;
import com.ideahub.backend.security.JwtTokenProvider;
import com.ideahub.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole(Role.ROLE_USER);

        User saved = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(saved);

        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(principal))
                .userId(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        UserPrincipal principal = new UserPrincipal(user);
        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(principal))
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
