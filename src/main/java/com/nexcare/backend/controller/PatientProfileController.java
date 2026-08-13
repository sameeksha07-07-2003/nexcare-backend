package com.nexcare.backend.controller;

import com.nexcare.backend.dto.PatientProfileResponse;
import com.nexcare.backend.dto.PatientProfileUpdateRequest;
import com.nexcare.backend.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(
            PatientProfileService patientProfileService) {

        this.patientProfileService = patientProfileService;
    }

    // GET /patient/profile
    // Returns the profile of the currently authenticated patient
    @GetMapping("/profile")
    public PatientProfileResponse getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Spring Security gives us the currently authenticated user
        UserDetails user = userDetails;

        // In our application, UserDetails.getUsername()
        // contains the user's email
        String email = user.getUsername();

        return patientProfileService.getProfile(email);
    }

    // PUT /patient/profile
    // Updates the profile of the currently authenticated patient
    @PutMapping("/profile")
    public PatientProfileResponse updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientProfileUpdateRequest request) {

        // Get the email of the currently authenticated user
        String email = userDetails.getUsername();

        // Pass the authenticated user's email and the
        // update data to the service layer
        return patientProfileService.updateProfile(email, request);
    }
}