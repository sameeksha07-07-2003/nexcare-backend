package com.nexcare.backend.controller;

import com.nexcare.backend.dto.AppointmentRequestDto;
import com.nexcare.backend.dto.AppointmentResponseDto;
import com.nexcare.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponseDto> bookAppointment(
            @Valid @RequestBody AppointmentRequestDto request,
            Authentication authentication
    ) {

        String patientEmail = authentication.getName();

        AppointmentResponseDto response =
                appointmentService.bookAppointment(request, patientEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}