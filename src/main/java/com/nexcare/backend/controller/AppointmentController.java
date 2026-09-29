package com.nexcare.backend.controller;

import com.nexcare.backend.dto.AppointmentCancellationRequest;
import com.nexcare.backend.dto.AppointmentRequestDto;
import com.nexcare.backend.dto.AppointmentResponseDto;
import com.nexcare.backend.dto.DoctorAppointmentScope;
import com.nexcare.backend.dto.DoctorAppointmentSummaryDto;
import com.nexcare.backend.entity.AppointmentStatus;
import com.nexcare.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponseDto>
    bookAppointment(
            @Valid
            @RequestBody
            AppointmentRequestDto request,
            Authentication authentication
    ) {
        AppointmentResponseDto response =
                appointmentService.bookAppointment(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public Page<AppointmentResponseDto>
    getMyAppointments(
            @RequestParam(required = false)
            AppointmentStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Authentication authentication
    ) {
        return appointmentService
                .getPatientAppointments(
                        authentication.getName(),
                        status,
                        page,
                        size
                );
    }

    @PatchMapping("/{appointmentId}/cancel")
    public AppointmentResponseDto cancelAppointment(
            @PathVariable Long appointmentId,

            @Valid
            @RequestBody
            AppointmentCancellationRequest request,

            Authentication authentication
    ) {
        return appointmentService.cancelAppointment(
                appointmentId,
                authentication.getName(),
                request
        );
    }

    @GetMapping("/doctor")
    public Page<AppointmentResponseDto>
    getDoctorAppointments(
            @RequestParam(required = false)
            AppointmentStatus status,

            @RequestParam(required = false)
            DoctorAppointmentScope scope,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Authentication authentication
    ) {
        return appointmentService
                .getDoctorAppointments(
                        authentication.getName(),
                        status,
                        scope,
                        page,
                        size
                );
    }

    @GetMapping("/doctor/summary")
    public DoctorAppointmentSummaryDto
    getDoctorAppointmentSummary(
            Authentication authentication
    ) {
        return appointmentService
                .getDoctorAppointmentSummary(
                        authentication.getName()
                );
    }

    @PatchMapping("/{appointmentId}/complete")
    public AppointmentResponseDto completeAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {
        return appointmentService
                .completeAppointment(
                        appointmentId,
                        authentication.getName()
                );
    }

    @PatchMapping("/{appointmentId}/no-show")
    public AppointmentResponseDto markAsNoShow(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {
        return appointmentService
                .markAppointmentAsNoShow(
                        appointmentId,
                        authentication.getName()
                );
    }
}