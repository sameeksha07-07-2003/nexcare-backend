package com.nexcare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Primary qualification is required")
    @Size(max = 255)
    private String primaryQualification;

    @Size(max = 255)
    private String additionalQualification;

    @NotBlank(message = "Specialization is required")
    @Size(max = 255)
    private String specialization;

    @NotNull(message = "Year of passing is required")
    @Positive(message = "Year of passing must be valid")
    private Integer yearOfPassing;

    @NotBlank(message = "Place of work is required")
    @Size(max = 255)
    private String placeOfWork;
}