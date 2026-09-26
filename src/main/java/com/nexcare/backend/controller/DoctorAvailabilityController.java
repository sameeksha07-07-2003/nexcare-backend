package com.nexcare.backend.controller;

import com.nexcare.backend.dto.DoctorAvailabilityRequest;
import com.nexcare.backend.dto.DoctorAvailabilityResponse;
import com.nexcare.backend.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctors/me/availability")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    public DoctorAvailabilityController(
            DoctorAvailabilityService doctorAvailabilityService
    ) {
        this.doctorAvailabilityService = doctorAvailabilityService;
    }

    @PostMapping
    public ResponseEntity<DoctorAvailabilityResponse> createAvailability(
            @Valid @RequestBody DoctorAvailabilityRequest request
    ) {
        DoctorAvailabilityResponse response =
                doctorAvailabilityService.createAvailability(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}