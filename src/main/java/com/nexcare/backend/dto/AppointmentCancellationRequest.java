package com.nexcare.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentCancellationRequest {

    @Size(
            max = 500,
            message = "Cancellation reason must not exceed 500 characters"
    )
    private String reason;
}