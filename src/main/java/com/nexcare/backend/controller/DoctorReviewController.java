package com.nexcare.backend.controller;

import com.nexcare.backend.dto.DoctorReviewRequest;
import com.nexcare.backend.dto.DoctorReviewResponse;
import com.nexcare.backend.service.DoctorReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DoctorReviewController {

    private final DoctorReviewService
            doctorReviewService;

    public DoctorReviewController(
            DoctorReviewService doctorReviewService
    ) {
        this.doctorReviewService =
                doctorReviewService;
    }

    @PostMapping(
            "/appointments/{appointmentId}/review"
    )
    public ResponseEntity<DoctorReviewResponse>
    createReview(
            @PathVariable Long appointmentId,

            @Valid
            @RequestBody
            DoctorReviewRequest request,

            Authentication authentication
    ) {
        DoctorReviewResponse response =
                doctorReviewService.createReview(
                        appointmentId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping(
            "/doctors/{doctorId}/reviews"
    )
    public Page<DoctorReviewResponse>
    getDoctorReviews(
            @PathVariable Long doctorId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return doctorReviewService
                .getDoctorReviews(
                        doctorId,
                        page,
                        size
                );
    }
}