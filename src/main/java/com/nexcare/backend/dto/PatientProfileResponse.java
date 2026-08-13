package com.nexcare.backend.dto;

import com.nexcare.backend.entity.BloodGroup;
import com.nexcare.backend.entity.Gender;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PatientProfileResponse {

    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;

    private final Gender gender;
    private final LocalDate dateOfBirth;
    private final BloodGroup bloodGroup;

    private final String address;
    private final String emergencyContact;

    private final Double height;
    private final Double weight;

    public PatientProfileResponse(
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            Gender gender,
            LocalDate dateOfBirth,
            BloodGroup bloodGroup,
            String address,
            String emergencyContact,
            Double height,
            Double weight) {

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;

        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.bloodGroup = bloodGroup;

        this.address = address;
        this.emergencyContact = emergencyContact;

        this.height = height;
        this.weight = weight;
    }
}