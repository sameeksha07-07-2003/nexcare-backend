package com.nexcare.backend.dto;

import com.nexcare.backend.entity.BloodGroup;
import com.nexcare.backend.entity.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileRequest {

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate dateOfBirth;

    private BloodGroup bloodGroup;

    private String address;

    private String emergencyContact;

    private Double height;

    private Double weight;
}