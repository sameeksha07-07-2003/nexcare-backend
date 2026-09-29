package com.nexcare.backend.dto;

import com.nexcare.backend.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DoctorProfileResponse {

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private String medicalRegistrationNumber;
    private String medicalCouncil;
    private LocalDate registrationDate;

    private String primaryQualification;
    private String additionalQualification;
    private String specialization;

    private Integer yearOfPassing;
    private String placeOfWork;
    private String city;
    private Integer yearsOfExperience;

    private String bio;
    private BigDecimal consultationFee;

    private String profileImageUrl;

    private BigDecimal averageRating;
    private Integer reviewCount;

    private VerificationStatus verificationStatus;
}
