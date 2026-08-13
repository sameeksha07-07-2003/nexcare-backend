package com.nexcare.backend.dto;

import com.nexcare.backend.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Role role;

    @Valid
    private PatientProfileRequest patientProfile;

    @Valid
    private DoctorProfileRequest doctorProfile;
}