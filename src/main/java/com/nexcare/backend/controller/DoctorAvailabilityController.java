package com.nexcare.backend.controller;

import com.nexcare.backend.config.BookingPolicy;
import com.nexcare.backend.dto.DoctorAvailabilityRequest;
import com.nexcare.backend.dto.DoctorAvailabilityResponse;
import com.nexcare.backend.dto.PublicDoctorAvailabilityResponse;
import com.nexcare.backend.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService
            doctorAvailabilityService;

    public DoctorAvailabilityController(
            DoctorAvailabilityService doctorAvailabilityService
    ) {
        this.doctorAvailabilityService =
                doctorAvailabilityService;
    }

    @PostMapping("/me/availability")
    public ResponseEntity<DoctorAvailabilityResponse>
    createAvailability(
            @Valid
            @RequestBody
            DoctorAvailabilityRequest request,
            Authentication authentication
    ) {
        DoctorAvailabilityResponse response =
                doctorAvailabilityService.createAvailability(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me/availability")
    public List<DoctorAvailabilityResponse>
    getMyAvailabilities(
            Authentication authentication
    ) {
        return doctorAvailabilityService
                .getMyAvailabilities(
                        authentication.getName()
                );
    }

    @PutMapping("/me/availability/{availabilityId}")
    public DoctorAvailabilityResponse updateAvailability(
            @PathVariable Long availabilityId,
            @Valid
            @RequestBody
            DoctorAvailabilityRequest request,
            Authentication authentication
    ) {
        return doctorAvailabilityService.updateAvailability(
                authentication.getName(),
                availabilityId,
                request
        );
    }

    @DeleteMapping("/me/availability/{availabilityId}")
    public ResponseEntity<Void> deactivateAvailability(
            @PathVariable Long availabilityId,
            Authentication authentication
    ) {
        doctorAvailabilityService.deactivateAvailability(
                authentication.getName(),
                availabilityId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(
            "/me/availability/{availabilityId}/reactivate"
    )
    public DoctorAvailabilityResponse reactivateAvailability(
            @PathVariable Long availabilityId,
            Authentication authentication
    ) {
        return doctorAvailabilityService
                .reactivateAvailability(
                        authentication.getName(),
                        availabilityId
                );
    }

    @GetMapping("/{doctorId}/availability")
    public List<PublicDoctorAvailabilityResponse>
    getPublicDoctorAvailability(
            @PathVariable Long doctorId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(
                    defaultValue = BookingPolicy.BOOKING_WINDOW_DAYS_TEXT
            )
            int days
    ) {
        return doctorAvailabilityService
                .getPublicAvailability(
                        doctorId,
                        startDate,
                        days
                );
    }
}
