package com.nexcare.backend.controller;

import com.nexcare.backend.dto.DoctorPublicDetailResponse;
import com.nexcare.backend.dto.DoctorPublicProfileResponse;
import com.nexcare.backend.service.DoctorDiscoveryService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.nexcare.backend.dto.DoctorFilterOptionsResponse;


@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorDiscoveryController {

    private final DoctorDiscoveryService
            doctorDiscoveryService;

    public DoctorDiscoveryController(
            DoctorDiscoveryService doctorDiscoveryService
    ) {
        this.doctorDiscoveryService =
                doctorDiscoveryService;
    }

    @GetMapping
    public Page<DoctorPublicProfileResponse>
    getDoctors(
            @RequestParam(required = false)
            String city,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Integer minExperience,

            @RequestParam(required = false)
            String qualification,

            @RequestParam(required = false)
            String specialization,

            @RequestParam(defaultValue = "rating")
            String sort,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return doctorDiscoveryService.findDoctors(
                city,
                search,
                minExperience,
                qualification,
                specialization,
                sort,
                page,
                size
        );
    }
    @GetMapping("/filter-options")
    public DoctorFilterOptionsResponse getFilterOptions() {
        return doctorDiscoveryService.getFilterOptions();
    }

    @GetMapping("/{doctorId}")
    public DoctorPublicDetailResponse
    getDoctorDetails(
            @PathVariable Long doctorId
    ) {
        return doctorDiscoveryService
                .getDoctorDetails(doctorId);
    }
}