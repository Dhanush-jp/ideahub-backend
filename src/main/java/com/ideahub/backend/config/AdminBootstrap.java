package com.ideahub.backend.config;

import com.ideahub.backend.model.Role;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    @Bean
    public CommandLineRunner createAdmin(UserRepository repo,
                                         PasswordEncoder encoder,
                                         @Value("${app.admin.username:admin123}") String adminUsername,
                                         @Value("${app.admin.email:admin123}") String adminEmail,
                                         @Value("${app.admin.password:admin123}") String adminPassword) {
        return args -> {
            if (repo.findByUsernameIgnoreCase(adminUsername).isEmpty()
                    && repo.findByEmailIgnoreCase(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(encoder.encode(adminPassword));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setBio("Admin account for IdeaHub AI console.");
                repo.save(admin);
            }
        };
    }
}
