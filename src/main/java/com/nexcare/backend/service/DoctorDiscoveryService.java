package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorPublicDetailResponse;
import com.nexcare.backend.dto.DoctorPublicProfileResponse;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.specification.DoctorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.nexcare.backend.dto.DoctorFilterOptionsResponse;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DoctorDiscoveryService {

    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final DoctorRepository doctorRepository;

    public DoctorDiscoveryService(
            DoctorRepository doctorRepository
    ) {
        this.doctorRepository = doctorRepository;
    }
    public DoctorFilterOptionsResponse getFilterOptions() {

        List<String> cities =
                doctorRepository
                        .findDistinctCitiesByVerificationStatus(
                                VerificationStatus.APPROVED
                        );

        List<String> specializations =
                doctorRepository
                        .findDistinctSpecializationsByVerificationStatus(
                                VerificationStatus.APPROVED
                        );

        List<String> primaryQualifications =
                doctorRepository
                        .findDistinctPrimaryQualificationsByVerificationStatus(
                                VerificationStatus.APPROVED
                        );

        List<String> additionalQualifications =
                doctorRepository
                        .findDistinctAdditionalQualificationsByVerificationStatus(
                                VerificationStatus.APPROVED
                        );

        List<String> qualifications =
                Stream.concat(
                                primaryQualifications.stream(),
                                additionalQualifications.stream()
                        )
                        .filter(value ->
                                value != null
                                        && !value.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .toList();

        return DoctorFilterOptionsResponse.builder()
                .cities(cities)
                .specializations(specializations)
                .qualifications(qualifications)
                .build();
    }

    public Page<DoctorPublicProfileResponse>
    findDoctors(
            String city,
            String search,
            Integer minimumExperience,
            String qualification,
            String specialization,
            String sort,
            int page,
            int size
    ) {
        validatePagination(page, size);

        Specification<Doctor> specification =
                Specification.where(
                        DoctorSpecification
                                .hasVerificationStatus(
                                        VerificationStatus.APPROVED
                                )
                );

        if (city != null && !city.isBlank()) {
            specification = specification.and(
                    DoctorSpecification.hasCity(city)
            );
        }

        if (specialization != null
                && !specialization.isBlank()) {

            specification = specification.and(
                    DoctorSpecification
                            .hasSpecialization(
                                    specialization
                            )
            );
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    DoctorSpecification
                            .matchesSearch(search)
            );
        }

        if (minimumExperience != null) {
            if (minimumExperience < 0) {
                throw new IllegalArgumentException(
                        "Minimum experience cannot be negative."
                );
            }

            specification = specification.and(
                    DoctorSpecification
                            .hasMinimumExperience(
                                    minimumExperience
                            )
            );
        }

        if (qualification != null
                && !qualification.isBlank()) {

            specification = specification.and(
                    DoctorSpecification
                            .hasQualification(
                                    qualification
                            )
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        createSort(sort)
                );

        return doctorRepository
                .findAll(specification, pageable)
                .map(this::toPublicProfileResponse);
    }

    /*
     * This overload keeps older code and tests compatible.
     */
    public Page<DoctorPublicProfileResponse>
    findDoctors(
            String city,
            String search,
            Integer minimumExperience,
            String qualification,
            int page,
            int size
    ) {
        return findDoctors(
                city,
                search,
                minimumExperience,
                qualification,
                null,
                "rating",
                page,
                size
        );
    }

    public DoctorPublicDetailResponse getDoctorDetails(
            Long doctorId
    ) {
        Doctor doctor = doctorRepository
                .findByDoctorIdAndVerificationStatus(
                        doctorId,
                        VerificationStatus.APPROVED
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Approved doctor not found."
                        )
                );

        return DoctorPublicDetailResponse.builder()
                .doctorId(doctor.getDoctorId())
                .firstName(
                        doctor.getUser().getFirstName()
                )
                .lastName(
                        doctor.getUser().getLastName()
                )
                .profileImageUrl(
                        doctor.getProfileImageUrl()
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .primaryQualification(
                        doctor.getPrimaryQualification()
                )
                .additionalQualification(
                        doctor.getAdditionalQualification()
                )
                .yearOfPassing(
                        doctor.getYearOfPassing()
                )
                .yearsOfExperience(
                        doctor.getYearsOfExperience()
                )
                .placeOfWork(
                        doctor.getPlaceOfWork()
                )
                .city(doctor.getCity())
                .bio(doctor.getBio())
                .consultationFee(
                        doctor.getConsultationFee()
                )
                .medicalCouncil(
                        doctor.getMedicalCouncil()
                )
                .verified(true)
                .averageRating(
                        doctor.getAverageRating()
                )
                .reviewCount(
                        doctor.getReviewCount()
                )
                .build();
    }

    private Sort createSort(String requestedSort) {
        String normalizedSort =
                requestedSort == null
                        ? "rating"
                        : requestedSort.trim()
                        .toLowerCase();

        return switch (normalizedSort) {
            case "rating" -> Sort.by(
                    Sort.Order.desc("averageRating"),
                    Sort.Order.desc("reviewCount"),
                    Sort.Order.asc("user.firstName")
            );

            case "experience" -> Sort.by(
                    Sort.Order.desc("yearsOfExperience")
                            .nullsLast(),
                    Sort.Order.desc("averageRating")
            );

            case "fee-low" -> Sort.by(
                    Sort.Order.asc("consultationFee")
                            .nullsLast(),
                    Sort.Order.desc("averageRating")
            );

            case "fee-high" -> Sort.by(
                    Sort.Order.desc("consultationFee")
                            .nullsLast(),
                    Sort.Order.desc("averageRating")
            );

            case "name" -> Sort.by(
                    Sort.Order.asc("user.firstName"),
                    Sort.Order.asc("user.lastName")
            );

            default -> throw new IllegalArgumentException(
                    "Invalid sort value. Supported values are: "
                            + "rating, experience, fee-low, "
                            + "fee-high and name."
            );
        };
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

    private DoctorPublicProfileResponse
    toPublicProfileResponse(
            Doctor doctor
    ) {
        return DoctorPublicProfileResponse.builder()
                .doctorId(doctor.getDoctorId())
                .firstName(
                        doctor.getUser().getFirstName()
                )
                .lastName(
                        doctor.getUser().getLastName()
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .primaryQualification(
                        doctor.getPrimaryQualification()
                )
                .additionalQualification(
                        doctor.getAdditionalQualification()
                )
                .yearsOfExperience(
                        doctor.getYearsOfExperience()
                )
                .placeOfWork(
                        doctor.getPlaceOfWork()
                )
                .city(doctor.getCity())
                .profileImageUrl(
                        doctor.getProfileImageUrl()
                )
                .consultationFee(
                        doctor.getConsultationFee()
                )
                .averageRating(
                        doctor.getAverageRating()
                )
                .reviewCount(
                        doctor.getReviewCount()
                )
                .build();
    }
}