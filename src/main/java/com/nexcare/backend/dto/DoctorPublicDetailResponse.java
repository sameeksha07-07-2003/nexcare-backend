package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DoctorPublicDetailResponse {

    private Long doctorId;

    private String firstName;
    private String lastName;

    private String profileImageUrl;

    private String specialization;
    private String primaryQualification;
    private String additionalQualification;

    private Integer yearOfPassing;
    private Integer yearsOfExperience;

    private String placeOfWork;
    private String city;

    private String bio;
    private BigDecimal consultationFee;

    private String medicalCouncil;
    private boolean verified;

    private BigDecimal averageRating;
    private Integer reviewCount;
}