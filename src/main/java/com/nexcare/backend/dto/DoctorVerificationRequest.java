package com.nexcare.backend.dto;

import com.nexcare.backend.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorVerificationRequest {

    @NotNull(message = "Verification status is required.")
    private VerificationStatus status;

    @Size(
            max = 1000,
            message = "Verification reason must not exceed 1000 characters."
    )
    private String reason;
}
