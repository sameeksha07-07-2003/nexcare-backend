package com.nexcare.backend.service;

import com.nexcare.backend.dto.AdminDashboardSummaryResponse;
import com.nexcare.backend.dto.AdminDoctorDetailResponse;
import com.nexcare.backend.dto.AdminDoctorListItemResponse;
import com.nexcare.backend.dto.DoctorVerificationRequest;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.Role;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.entity.UserStatus;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.exception.ResourceNotFoundException;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.UserRepository;
import com.nexcare.backend.specification.DoctorSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminDoctorService {

    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public AdminDoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdminDashboardSummaryResponse getSummary() {
        return AdminDashboardSummaryResponse.builder()
                .totalDoctors(doctorRepository.count())
                .pendingDoctors(
                        doctorRepository.countByVerificationStatus(
                                VerificationStatus.PENDING
                        )
                )
                .approvedDoctors(
                        doctorRepository.countByVerificationStatus(
                                VerificationStatus.APPROVED
                        )
                )
                .rejectedDoctors(
                        doctorRepository.countByVerificationStatus(
                                VerificationStatus.REJECTED
                        )
                )
                .build();
    }

    @Transactional
    public Page<AdminDoctorListItemResponse> getDoctors(
            VerificationStatus status,
            String search,
            int page,
            int size
    ) {
        validatePagination(page, size);

        Specification<Doctor> specification =
                (root, query, builder) ->
                        builder.conjunction();

        if (status != null) {
            specification = specification.and(
                    DoctorSpecification.hasVerificationStatus(status)
            );
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    DoctorSpecification.matchesAdminSearch(search)
            );
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return doctorRepository
                .findAll(specification, pageRequest)
                .map(this::toListItemResponse);
    }

    @Transactional
    public AdminDoctorDetailResponse getDoctor(Long doctorId) {
        return toDetailResponse(findDoctor(doctorId));
    }

    @Transactional
    public AdminDoctorDetailResponse verifyDoctor(
            Long doctorId,
            DoctorVerificationRequest request,
            String adminEmail
    ) {
        User admin = findActiveAdmin(adminEmail);
        VerificationStatus targetStatus = request.getStatus();

        if (targetStatus != VerificationStatus.APPROVED
                && targetStatus != VerificationStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Verification status must be APPROVED or REJECTED."
            );
        }

        String reason = normalizeReason(request.getReason());

        if (targetStatus == VerificationStatus.REJECTED
                && reason == null) {
            throw new IllegalArgumentException(
                    "A rejection reason is required."
            );
        }

        Doctor doctor = doctorRepository
                .findByIdForVerificationUpdate(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found."
                ));

        if (doctor.getVerificationStatus() == targetStatus) {
            throw new IllegalStateException(
                    "Doctor is already "
                            + targetStatus.name().toLowerCase()
                            + "."
            );
        }

        doctor.setVerificationStatus(targetStatus);
        doctor.setVerificationReason(
                targetStatus == VerificationStatus.REJECTED
                        ? reason
                        : null
        );
        doctor.setVerificationReviewedAt(LocalDateTime.now());
        doctor.setVerificationReviewedBy(admin);

        return toDetailResponse(doctorRepository.save(doctor));
    }

    private User findActiveAdmin(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException(
                        "Authenticated administrator was not found."
                ));

        if (admin.getRole() != Role.ADMIN
                || admin.getStatus() != UserStatus.ACTIVE
                || admin.getDeletedAt() != null) {
            throw new SecurityException(
                    "You are not allowed to perform this operation."
            );
        }

        return admin;
    }

    private Doctor findDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException(
                    "A valid doctor ID is required."
            );
        }

        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found."
                ));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and "
                            + MAXIMUM_PAGE_SIZE
                            + "."
            );
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }

    private AdminDoctorListItemResponse toListItemResponse(
            Doctor doctor
    ) {
        User user = doctor.getUser();

        return AdminDoctorListItemResponse.builder()
                .doctorId(doctor.getDoctorId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .specialization(doctor.getSpecialization())
                .primaryQualification(
                        doctor.getPrimaryQualification()
                )
                .medicalRegistrationNumber(
                        doctor.getMedicalRegistrationNumber()
                )
                .city(doctor.getCity())
                .profileImageUrl(doctor.getProfileImageUrl())
                .verificationStatus(
                        doctor.getVerificationStatus()
                )
                .submittedAt(doctor.getCreatedAt())
                .reviewedAt(doctor.getVerificationReviewedAt())
                .build();
    }

    private AdminDoctorDetailResponse toDetailResponse(
            Doctor doctor
    ) {
        User user = doctor.getUser();
        User reviewer = doctor.getVerificationReviewedBy();

        return AdminDoctorDetailResponse.builder()
                .doctorId(doctor.getDoctorId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .medicalRegistrationNumber(
                        doctor.getMedicalRegistrationNumber()
                )
                .medicalCouncil(doctor.getMedicalCouncil())
                .registrationDate(doctor.getRegistrationDate())
                .primaryQualification(
                        doctor.getPrimaryQualification()
                )
                .additionalQualification(
                        doctor.getAdditionalQualification()
                )
                .specialization(doctor.getSpecialization())
                .yearOfPassing(doctor.getYearOfPassing())
                .placeOfWork(doctor.getPlaceOfWork())
                .city(doctor.getCity())
                .yearsOfExperience(
                        doctor.getYearsOfExperience()
                )
                .bio(doctor.getBio())
                .consultationFee(doctor.getConsultationFee())
                .profileImageUrl(doctor.getProfileImageUrl())
                .verificationStatus(
                        doctor.getVerificationStatus()
                )
                .verificationReason(
                        doctor.getVerificationReason()
                )
                .submittedAt(doctor.getCreatedAt())
                .reviewedAt(doctor.getVerificationReviewedAt())
                .reviewedByEmail(
                        reviewer == null ? null : reviewer.getEmail()
                )
                .build();
    }
}
