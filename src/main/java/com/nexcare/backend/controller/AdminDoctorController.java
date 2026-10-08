package com.nexcare.backend.controller;

import com.nexcare.backend.dto.AdminDashboardSummaryResponse;
import com.nexcare.backend.dto.AdminDoctorDetailResponse;
import com.nexcare.backend.dto.AdminDoctorListItemResponse;
import com.nexcare.backend.dto.DoctorVerificationRequest;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.service.AdminDoctorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    public AdminDoctorController(
            AdminDoctorService adminDoctorService
    ) {
        this.adminDoctorService = adminDoctorService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminDoctorService.getSummary();
    }

    @GetMapping("/doctors")
    public Page<AdminDoctorListItemResponse> getDoctors(
            @RequestParam(required = false)
            VerificationStatus status,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return adminDoctorService.getDoctors(
                status,
                search,
                page,
                size
        );
    }

    @GetMapping("/doctors/{doctorId}")
    public AdminDoctorDetailResponse getDoctor(
            @PathVariable Long doctorId
    ) {
        return adminDoctorService.getDoctor(doctorId);
    }

    @PatchMapping("/doctors/{doctorId}/verification")
    public AdminDoctorDetailResponse verifyDoctor(
            @PathVariable Long doctorId,
            @Valid @RequestBody
            DoctorVerificationRequest request,
            Authentication authentication
    ) {
        return adminDoctorService.verifyDoctor(
                doctorId,
                request,
                authentication.getName()
        );
    }
}
