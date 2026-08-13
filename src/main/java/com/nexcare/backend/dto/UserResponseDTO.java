package com.nexcare.backend.dto;

import com.nexcare.backend.entity.Role;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.entity.UserStatus;

import java.time.LocalDateTime;

/**
 * Safe, client-facing representation of a User.
 * Never include passwordHash (or any other sensitive field) here.
 */
public class UserResponseDTO {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Role role;
    private final UserStatus status;
    private final LocalDateTime createdAt;

    private UserResponseDTO(Long id, String firstName, String lastName, String email,
                             Role role, UserStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
