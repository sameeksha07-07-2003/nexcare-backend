package com.nexcare.backend.dto;

import com.nexcare.backend.entity.BloodGroup;
import com.nexcare.backend.entity.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileUpdateRequest {

    private Gender gender;

    private LocalDate dateOfBirth;


    private BloodGroup bloodGroup;

    private String address;

    private String emergencyContact;

    private Double height;

    private Double weight;
}