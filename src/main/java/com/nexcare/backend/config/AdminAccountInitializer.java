package com.nexcare.backend.config;

import com.nexcare.backend.entity.Role;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.entity.UserStatus;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final int MINIMUM_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapEnabled;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFirstName;
    private final String adminLastName;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap.enabled:false}")
            boolean bootstrapEnabled,
            @Value("${app.admin.bootstrap.email:}")
            String adminEmail,
            @Value("${app.admin.bootstrap.password:}")
            String adminPassword,
            @Value("${app.admin.bootstrap.first-name:NexCare}")
            String adminFirstName,
            @Value("${app.admin.bootstrap.last-name:Administrator}")
            String adminLastName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEnabled = bootstrapEnabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (!bootstrapEnabled) {
            return;
        }

        String normalizedEmail = normalizeEmail(adminEmail);
        validateConfiguration(normalizedEmail);

        userRepository.findByEmail(normalizedEmail)
                .ifPresentOrElse(
                        this::validateExistingAdmin,
                        () -> createAdmin(normalizedEmail)
                );
    }

    private void createAdmin(String normalizedEmail) {
        User admin = new User();
        admin.setFirstName(normalizeName(
                adminFirstName,
                "NexCare"
        ));
        admin.setLastName(normalizeName(
                adminLastName,
                "Administrator"
        ));
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.save(admin);
    }

    private void validateExistingAdmin(User existingUser) {
        if (existingUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException(
                    "The configured admin email belongs to a non-admin account."
            );
        }
    }

    private void validateConfiguration(String normalizedEmail) {
        if (normalizedEmail == null
                || !normalizedEmail.contains("@")) {
            throw new IllegalStateException(
                    "A valid ADMIN_EMAIL is required when admin bootstrap is enabled."
            );
        }

        if (adminPassword == null
                || adminPassword.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD must contain at least "
                            + MINIMUM_PASSWORD_LENGTH
                            + " characters."
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(
            String value,
            String fallback
    ) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim();
    }
}
