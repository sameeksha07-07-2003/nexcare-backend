package com.nexcare.backend.controller;

import com.nexcare.backend.dto.CloudinaryUploadResultRequest;
import com.nexcare.backend.dto.PatientProfileResponse;
import com.nexcare.backend.dto.PatientProfileUpdateRequest;
import com.nexcare.backend.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientProfileController {

    private final PatientProfileService
            patientProfileService;

    public PatientProfileController(
            PatientProfileService patientProfileService
    ) {
        this.patientProfileService =
                patientProfileService;
    }

    @GetMapping("/profile")
    public PatientProfileResponse getProfile(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return patientProfileService.getProfile(
                userDetails.getUsername()
        );
    }

    @PutMapping("/profile")
    public PatientProfileResponse updateProfile(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            PatientProfileUpdateRequest request
    ) {
        return patientProfileService.updateProfile(
                userDetails.getUsername(),
                request
        );
    }

    @GetMapping(
            "/profile/photo-upload-signature"
    )
    public Map<String, Object>
    getPhotoUploadSignature(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return patientProfileService
                .generatePhotoUploadSignature(
                        userDetails.getUsername()
                );
    }

    @PutMapping("/profile/photo")
    public PatientProfileResponse updateProfilePhoto(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            CloudinaryUploadResultRequest request
    ) {
        return patientProfileService.updateProfilePhoto(
                userDetails.getUsername(),
                request
        );
    }
}