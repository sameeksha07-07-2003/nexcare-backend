package com.nexcare.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {

    private Long appointmentId;

    private String doctorName;

    private LocalDate appointmentDate;

    private String timeWindow;

    private int queueNumber;

    private String status;
}