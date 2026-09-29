package com.nexcare.backend.dto;

import com.nexcare.backend.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 320)
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank
    @Size(max = 20)
    @Pattern(
            regexp = "^[0-9+() -]{7,20}$",
            message = "Phone number is invalid"
    )
    private String phoneNumber;

    @NotNull
    private Role role;

    @Valid
    private PatientProfileRequest patientProfile;

    @Valid
    private DoctorProfileRequest doctorProfile;
}
