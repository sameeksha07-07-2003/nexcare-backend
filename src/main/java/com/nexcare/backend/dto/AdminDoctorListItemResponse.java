package com.nexcare.backend.dto;

import com.nexcare.backend.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminDoctorListItemResponse {

    private Long doctorId;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String primaryQualification;
    private String medicalRegistrationNumber;
    private String city;
    private String profileImageUrl;
    private VerificationStatus verificationStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
