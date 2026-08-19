package com.nexcare.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DoctorPublicProfileResponse {

    private Long doctorId;

    private String firstName;
    private String lastName;

    private String specialization;

    private String primaryQualification;
    private String additionalQualification;

    private Integer yearsOfExperience;

    private String placeOfWork;
    private String city;
}