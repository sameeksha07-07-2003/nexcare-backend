package com.nexcare.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AppointmentRequestDto {

    @NotNull(message = "Doctor availability is required")
    private Long doctorAvailabilityId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be today or later")
    private LocalDate appointmentDate;

    @Size(
            max = 500,
            message = "Reason for visit must not exceed 500 characters"
    )
    private String reasonForVisit;
}