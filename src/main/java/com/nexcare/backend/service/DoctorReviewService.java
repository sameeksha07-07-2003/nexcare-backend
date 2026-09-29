package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorReviewRequest;
import com.nexcare.backend.dto.DoctorReviewResponse;
import com.nexcare.backend.entity.*;
import com.nexcare.backend.repository.AppointmentRepository;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.DoctorReviewRepository;
import com.nexcare.backend.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DoctorReviewService {

    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final DoctorReviewRepository
            doctorReviewRepository;

    private final AppointmentRepository
            appointmentRepository;

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public DoctorReviewService(
            DoctorReviewRepository doctorReviewRepository,
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository
    ) {
        this.doctorReviewRepository =
                doctorReviewRepository;

        this.appointmentRepository =
                appointmentRepository;

        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public DoctorReviewResponse createReview(
            Long appointmentId,
            String patientEmail,
            DoctorReviewRequest request
    ) {
        Patient patient =
                patientRepository
                        .findByUserEmail(patientEmail)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Patient profile not found."
                                )
                        );

        /*
         * Locking the appointment ensures that two simultaneous review
         * requests cannot both pass the duplicate-review validation.
         */
        Appointment appointment =
                appointmentRepository
                        .findByIdForUpdate(appointmentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Appointment not found."
                                )
                        );

        if (!appointment.getPatient()
                .getPatientId()
                .equals(patient.getPatientId())) {

            throw new SecurityException(
                    "You are not allowed to review this appointment."
            );
        }

        if (appointment.getStatus()
                != AppointmentStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Only completed appointments can be reviewed."
            );
        }

        if (doctorReviewRepository
                .existsByAppointment(appointment)) {

            throw new IllegalStateException(
                    "A review has already been submitted "
                            + "for this appointment."
            );
        }

        Long doctorId =
                appointment.getAvailabilityOccurrence()
                        .getDoctorAvailability()
                        .getDoctor()
                        .getDoctorId();

        /*
         * Lock the Doctor row while updating averageRating and
         * reviewCount. This prevents lost rating updates when two
         * patients submit reviews simultaneously.
         */
        Doctor doctor =
                doctorRepository
                        .findByIdForUpdate(doctorId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor profile not found."
                                )
                        );

        DoctorReview review = new DoctorReview();

        review.setDoctor(doctor);
        review.setPatient(patient);
        review.setAppointment(appointment);
        review.setRating(request.getRating());

        review.setComment(
                normalizeOptionalText(request.getComment())
        );

        DoctorReview savedReview =
                doctorReviewRepository.save(review);

        updateDoctorRatingSummary(
                doctor,
                request.getRating()
        );

        return toResponse(savedReview);
    }

    @Transactional
    public Page<DoctorReviewResponse> getDoctorReviews(
            Long doctorId,
            int page,
            int size
    ) {
        validatePagination(page, size);

        Doctor doctor =
                doctorRepository
                        .findByDoctorIdAndVerificationStatus(
                                doctorId,
                                VerificationStatus.APPROVED
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Approved doctor not found."
                                )
                        );

        return doctorReviewRepository
                .findByDoctorOrderByCreatedAtDesc(
                        doctor,
                        PageRequest.of(page, size)
                )
                .map(this::toResponse);
    }

    private void updateDoctorRatingSummary(
            Doctor doctor,
            int newRating
    ) {
        int currentReviewCount =
                doctor.getReviewCount() == null
                        ? 0
                        : doctor.getReviewCount();

        BigDecimal currentAverage =
                doctor.getAverageRating() == null
                        ? BigDecimal.ZERO
                        : doctor.getAverageRating();

        BigDecimal currentTotal =
                currentAverage.multiply(
                        BigDecimal.valueOf(
                                currentReviewCount
                        )
                );

        int newReviewCount =
                currentReviewCount + 1;

        BigDecimal newAverage =
                currentTotal
                        .add(
                                BigDecimal.valueOf(newRating)
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        newReviewCount
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        doctor.setReviewCount(newReviewCount);
        doctor.setAverageRating(newAverage);

        doctorRepository.save(doctor);
    }

    private DoctorReviewResponse toResponse(
            DoctorReview review
    ) {
        return DoctorReviewResponse.builder()
                .reviewId(review.getReviewId())
                .doctorId(
                        review.getDoctor().getDoctorId()
                )
                .patientName(
                        review.getPatient()
                                .getUser()
                                .getFirstName()
                )
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private void validatePagination(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50."
            );
        }
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}