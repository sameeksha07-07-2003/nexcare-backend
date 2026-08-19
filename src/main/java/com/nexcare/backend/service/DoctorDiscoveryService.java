package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorPublicProfileResponse;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.specification.DoctorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DoctorDiscoveryService {

    // Repository used to execute database queries related to Doctor entities.
    private final DoctorRepository doctorRepository;

    // Constructor injection is used to provide DoctorRepository to this service.
    public DoctorDiscoveryService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * Finds doctors based on optional search and filter parameters.
     *
     * Business rule:
     * Only doctors with APPROVED verification status can appear
     * in the Doctor Discovery results.
     *
     * Optional filters:
     * - city
     * - search
     * - minimum experience
     * - qualification
     *
     * Results are returned using pagination.
     *
     * @return a paginated list of safe DoctorPublicProfileResponse DTOs
     */
    public Page<DoctorPublicProfileResponse> findDoctors(
            String city,
            String search,
            Integer minExperience,
            String qualification,
            int page,
            int size
    ) {

        /*
         * ============================================================
         * STEP 1: START WITH THE MANDATORY BASE FILTER
         * ============================================================
         *
         * Regardless of which filters the user provides,
         * Doctor Discovery must only show APPROVED doctors.
         *
         * This is the base Specification on which all optional
         * filters will be added.
         */
        Specification<Doctor> spec = Specification.where(
                DoctorSpecification.hasVerificationStatus(
                        VerificationStatus.APPROVED
                )
        );


        /*
         * ============================================================
         * STEP 2: ADD CITY FILTER
         * ============================================================
         *
         * City is optional.
         *
         * If the user provides a meaningful city value, combine
         * the city condition with the existing Specification.
         *
         * Example:
         * APPROVED AND city = "Bhopal"
         */
        if (city != null && !city.isBlank()) {
            spec = spec.and(
                    DoctorSpecification.hasCity(city)
            );
        }


        /*
         * ============================================================
         * STEP 3: ADD SEARCH FILTER
         * ============================================================
         *
         * Search is optional.
         *
         * The matchesSearch Specification searches across fields
         * such as:
         * - doctor's first name
         * - doctor's last name
         * - specialization
         *
         * Example:
         * search = "Rahul"
         *
         * APPROVED
         * AND city = "Bhopal"
         * AND (firstName OR lastName OR specialization matches "Rahul")
         */
        if (search != null && !search.isBlank()) {
            spec = spec.and(
                    DoctorSpecification.matchesSearch(search)
            );
        }


        /*
         * ============================================================
         * STEP 4: ADD MINIMUM EXPERIENCE FILTER
         * ============================================================
         *
         * This filter is optional.
         *
         * Example:
         * minExperience = 5
         *
         * Only doctors having 5 or more years of experience
         * will match.
         *
         * Conceptually:
         * yearsOfExperience >= 5
         */
        if (minExperience != null) {
            spec = spec.and(
                    DoctorSpecification.hasMinimumExperience(
                            minExperience
                    )
            );
        }


        /*
         * ============================================================
         * STEP 5: ADD QUALIFICATION FILTER
         * ============================================================
         *
         * Qualification is optional.
         *
         * The qualification Specification can search in fields such as:
         * - primaryQualification
         * - additionalQualification
         *
         * Example:
         * qualification = "MBBS"
         */
        if (qualification != null && !qualification.isBlank()) {
            spec = spec.and(
                    DoctorSpecification.hasQualification(
                            qualification
                    )
            );
        }


        /*
         * ============================================================
         * STEP 6: CREATE PAGINATION INSTRUCTIONS
         * ============================================================
         *
         * PageRequest.of(page, size) creates a Pageable object.
         *
         * Example:
         * page = 0
         * size = 10
         *
         * This means:
         * Fetch the first page containing up to 10 doctors.
         *
         * Spring page numbering starts from 0.
         *
         * page = 0 -> first page
         * page = 1 -> second page
         * page = 2 -> third page
         */
        Pageable pageable = PageRequest.of(page, size);


        /*
         * ============================================================
         * STEP 7: EXECUTE THE DATABASE QUERY
         * ============================================================
         *
         * We pass two things to the repository:
         *
         * 1. spec
         *    -> tells the database WHAT conditions to apply
         *
         * 2. pageable
         *    -> tells Spring HOW MANY records to return and WHICH page
         *
         * Result:
         * Page<Doctor>
         *
         * Page contains:
         * - list of Doctor entities for the current page
         * - current page information
         * - page size
         * - total number of matching doctors
         * - total number of pages
         */
        Page<Doctor> doctorPage =
                doctorRepository.findAll(spec, pageable);


        /*
         * ============================================================
         * STEP 8: MAP ENTITY TO PUBLIC RESPONSE DTO
         * ============================================================
         *
         * We should not directly expose the Doctor entity from the API.
         *
         * The entity may contain internal or sensitive fields such as:
         * - medical registration number
         * - medical council
         * - verification status
         * - relationship with User
         *
         * Therefore, we convert each Doctor entity into
         * DoctorPublicProfileResponse.
         *
         * doctorPage.map() performs this conversion for every Doctor
         * in the current page.
         *
         * IMPORTANT:
         * Only the content changes from Doctor -> DTO.
         * Pagination metadata is automatically preserved.
         *
         * Page<Doctor>
         *        |
         *        | map()
         *        v
         * Page<DoctorPublicProfileResponse>
         */
        return doctorPage.map(doctor ->
                new DoctorPublicProfileResponse(

                        // Doctor's unique ID
                        doctor.getDoctorId(),

                        // Name is stored inside the related User entity
                        doctor.getUser().getFirstName(),
                        doctor.getUser().getLastName(),

                        // Professional information
                        doctor.getSpecialization(),
                        doctor.getPrimaryQualification(),
                        doctor.getAdditionalQualification(),

                        // Experience information
                        doctor.getYearsOfExperience(),

                        // Practice/location information
                        doctor.getPlaceOfWork(),
                        doctor.getCity()
                )
        );
    }
}