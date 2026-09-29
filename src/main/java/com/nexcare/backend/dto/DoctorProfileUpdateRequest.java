package com.nexcare.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank(message = "Primary qualification is required")
    @Size(max = 255)
    private String primaryQualification;

    @Size(max = 255)
    private String additionalQualification;

    @NotBlank(message = "Specialization is required")
    @Size(max = 150)
    private String specialization;

    @NotNull(message = "Year of passing is required")
    @Min(value = 1950, message = "Year of passing is invalid")
    private Integer yearOfPassing;

    @NotBlank(message = "Place of work is required")
    @Size(max = 255)
    private String placeOfWork;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotNull(message = "Years of experience is required")
    @PositiveOrZero(message = "Years of experience cannot be negative")
    @Max(value = 80, message = "Years of experience is invalid")
    private Integer yearsOfExperience;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Consultation fee cannot be negative"
    )
    private BigDecimal consultationFee;
}