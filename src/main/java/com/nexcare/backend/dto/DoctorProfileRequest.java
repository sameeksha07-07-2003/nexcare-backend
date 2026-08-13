package com.nexcare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DoctorProfileRequest {

    @NotBlank
    private String medicalRegistrationNumber;

    @NotBlank
    private String medicalCouncil;

    @NotNull
    private LocalDate registrationDate;

    @NotBlank
    private String primaryQualification;

    private String additionalQualification;

    private String specialization;

    private Integer yearOfPassing;

    private String placeOfWork;
}