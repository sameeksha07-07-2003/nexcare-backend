package com.nexcare.backend.controller;

import com.nexcare.backend.dto.DoctorPublicProfileResponse;
import com.nexcare.backend.service.DoctorDiscoveryService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST Controller for Doctor Discovery.
 *
 * Base URL:
 * /api/v1/doctors
 *
 * This controller is responsible for:
 * - receiving HTTP requests
 * - reading query parameters
 * - passing those parameters to the service layer
 * - returning the service result to the client
 *
 * Business logic should remain inside DoctorDiscoveryService.
 */
@RestController
@RequestMapping("/api/v1")
public class DoctorDiscoveryController {
    private final DoctorDiscoveryService doctorDiscoveryService;

    public DoctorDiscoveryController(DoctorDiscoveryService doctorDiscoveryService){
        this.doctorDiscoveryService = doctorDiscoveryService;
    }

    /**
     * Doctor Discovery API.
     *
     * Endpoint:
     * GET /api/v1/doctors
     *
     * Example:
     * GET /api/v1/doctors?city=Bhopal
     *
     * Example with multiple filters:
     * GET /api/v1/doctors
     *     ?city=Bhopal
     *     &search=Rahul
     *     &minExperience=5
     *     &qualification=MBBS
     *     &page=0
     *     &size=10
     */
    @GetMapping("/doctors")
    public Page<DoctorPublicProfileResponse> getDoctors(
            /**
              *@RequestParam(required = false) makes the parameter optional
            */
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) String qualification,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return doctorDiscoveryService.findDoctors(city,search,minExperience,qualification,page,size);
    }
}
