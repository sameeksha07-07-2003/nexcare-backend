package com.nexcare.backend.dto;

import com.nexcare.backend.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class DoctorProfileResponse {
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;

    private final String additionalQualification;
    private final String placeOfWork;
    private final String specialization;
    private final String medicalCouncil;
    private final String medicalRegistrationNumber;
    private final String primaryQualification;

    private final VerificationStatus verificationStatus;
    private final Integer yearOfPassing;

}
