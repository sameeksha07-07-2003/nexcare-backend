package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorReviewResponse {

    private Long reviewId;

    private Long doctorId;

    /*
     * Only the patient's first name is exposed publicly.
     */
    private String patientName;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
}