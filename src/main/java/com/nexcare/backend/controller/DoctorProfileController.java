package com.nexcare.backend.controller;

import com.nexcare.backend.dto.CloudinaryUploadResultRequest;
import com.nexcare.backend.dto.DoctorProfileResponse;
import com.nexcare.backend.dto.DoctorProfileUpdateRequest;
import com.nexcare.backend.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final DoctorProfileService
            doctorProfileService;

    public DoctorProfileController(
            DoctorProfileService doctorProfileService
    ) {
        this.doctorProfileService =
                doctorProfileService;
    }

    @GetMapping("/profile")
    public DoctorProfileResponse getProfile(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return doctorProfileService.getProfile(
                userDetails.getUsername()
        );
    }

    @PutMapping("/profile")
    public DoctorProfileResponse updateProfile(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            DoctorProfileUpdateRequest request
    ) {
        return doctorProfileService.updateProfile(
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
        return doctorProfileService
                .generatePhotoUploadSignature(
                        userDetails.getUsername()
                );
    }

    @PutMapping("/profile/photo")
    public DoctorProfileResponse updateProfilePhoto(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            CloudinaryUploadResultRequest request
    ) {
        return doctorProfileService.updateProfilePhoto(
                userDetails.getUsername(),
                request
        );
    }
}