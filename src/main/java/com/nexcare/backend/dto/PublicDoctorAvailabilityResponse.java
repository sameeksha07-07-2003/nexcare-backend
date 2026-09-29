package com.nexcare.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class PublicDoctorAvailabilityResponse {

    private Long doctorAvailabilityId;

    private LocalDate appointmentDate;
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private Integer maxPatients;
    private Integer bookedPatients;
    private Integer remainingCapacity;

    private boolean fullyBooked;
}
