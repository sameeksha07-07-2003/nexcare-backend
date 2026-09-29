package com.nexcare.backend.dto;

import com.nexcare.backend.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class AppointmentResponseDto {

    private Long appointmentId;

    private Long doctorId;
    private String doctorName;
    private String doctorProfileImageUrl;
    private String specialization;

    private Long patientId;
    private String patientName;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private int queueNumber;

    private AppointmentStatus status;

    private String reasonForVisit;
    private String cancellationReason;

    private boolean reviewSubmitted;
}