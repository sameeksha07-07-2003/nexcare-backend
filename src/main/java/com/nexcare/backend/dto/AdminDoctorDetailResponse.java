package com.nexcare.backend.dto;

import com.nexcare.backend.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminDoctorDetailResponse {

    private Long doctorId;
    private String firstName;
    private String lastName;
    private String email;
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
    private VerificationStatus verificationStatus;
    private String verificationReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByEmail;
}
