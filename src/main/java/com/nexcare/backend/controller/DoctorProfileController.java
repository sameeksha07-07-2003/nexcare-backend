package com.nexcare.backend.controller;

import com.nexcare.backend.dto.DoctorProfileResponse;
import com.nexcare.backend.dto.DoctorProfileUpdateRequest;
import com.nexcare.backend.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    public DoctorProfileController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping("/profile")
    public DoctorProfileResponse getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();

        return doctorProfileService.getProfile(email);
    }

    @PutMapping("/profile")
    public DoctorProfileResponse updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorProfileUpdateRequest request
    ) {
        String email = userDetails.getUsername();

        return doctorProfileService.updateProfile(email, request);
    }
}