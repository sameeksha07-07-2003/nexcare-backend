package com.nexcare.backend.specification;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.VerificationStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class responsible for creating reusable and dynamic
 * JPA Specification filters for the Doctor Discovery feature.
 *
 * Each method represents one database filtering condition.
 *
 * These conditions can later be combined dynamically using AND/OR.
 *
 * Example:
 *
 * APPROVED
 * AND city = Bhopal
 * AND yearsOfExperience >= 5
 * AND (name contains "rahul" OR specialization contains "rahul")
 */
public class DoctorSpecification {

    /**
     * Private constructor prevents object creation.
     *
     * We do not need:
     * new DoctorSpecification()
     *
     * because all methods in this class are static utility methods.
     */
    private DoctorSpecification() {
    }


    /**
     * Creates a filter based on doctor's verification status.
     *
     * Example:
     * hasVerificationStatus(APPROVED)
     *
     * Conceptually generates:
     *
     * WHERE verification_status = 'APPROVED'
     *
     * This is an important base filter because Doctor Discovery
     * must never show PENDING or REJECTED doctors.
     */
    public static Specification<Doctor> hasVerificationStatus(
            VerificationStatus status
    ) {

        return (root, query, criteriaBuilder) ->

                // root represents the Doctor entity being queried.
                // root.get("verificationStatus") accesses the
                // verificationStatus field of Doctor.
                //
                // criteriaBuilder.equal(...) creates an equality condition.
                criteriaBuilder.equal(
                        root.get("verificationStatus"),
                        status
                );
    }


    /**
     * Creates a city filter using case-insensitive exact matching.
     *
     * Example:
     * hasCity("Bhopal")
     *
     * Conceptually generates:
     *
     * WHERE LOWER(city) = 'bhopal'
     *
     * This allows "Bhopal", "BHOPAL", and "bhopal"
     * to match the same doctors.
     *
     * If city is null or blank, null is returned.
     * This means no city condition should be applied when
     * specifications are combined.
     *
     * This supports our Doctor Discovery behavior where
     * city can be optional and the API can fall back to
     * a nationwide search.
     */
    public static Specification<Doctor> hasCity(String city) {

        // No city available -> do not add a city filter.
        if (city == null || city.isBlank()) {
            return null;
        }

        return (root, query, criteriaBuilder) ->

                // Convert the database city value to lowercase.
                // Also trim and lowercase the input city.
                // This provides case-insensitive exact matching.
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("city")),
                        city.trim().toLowerCase()
                );
    }


    /**
     * Creates a minimum experience filter.
     *
     * Example:
     * hasMinimumExperience(5)
     *
     * Conceptually generates:
     *
     * WHERE years_of_experience >= 5
     *
     * Therefore:
     * Experience = 3 -> Not included
     * Experience = 5 -> Included
     * Experience = 10 -> Included
     *
     * If minExperience is not provided, no experience filter
     * is applied.
     */
    public static Specification<Doctor> hasMinimumExperience(
            Integer minExperience
    ) {

        // Filter is optional.
        if (minExperience == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->

                // greaterThanOrEqualTo creates:
                // yearsOfExperience >= minExperience
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("yearsOfExperience"),
                        minExperience
                );
    }


    /**
     * Creates a case-insensitive partial text search filter.
     *
     * The search term is checked against:
     *
     * 1. Doctor's first name
     * 2. Doctor's last name
     * 3. Doctor's specialization
     *
     * These three search conditions are connected using OR.
     *
     * Example:
     * matchesSearch("rah")
     *
     * Can match:
     * Rahul Sharma
     * Dr. Prakash (if "rah" exists in a searched field)
     * A specialization containing "rah"
     *
     * The '%' characters create partial matching.
     *
     * "%rah%" means:
     * Any text before "rah"
     * + "rah"
     * + Any text after "rah"
     *
     * This is conceptually similar to:
     *
     * LOWER(first_name) LIKE '%rah%'
     * OR LOWER(last_name) LIKE '%rah%'
     * OR LOWER(specialization) LIKE '%rah%'
     *
     * If search is null or blank, no search filter is applied.
     */
    public static Specification<Doctor> matchesSearch(String search) {

        // Search filter is optional.
        if (search == null || search.isBlank()) {
            return null;
        }

        return (root, query, criteriaBuilder) -> {

            // Normalize input and create a pattern for partial matching.
            //
            // Example:
            // search = "Rah"
            // pattern = "%rah%"
            //
            // trim() removes unnecessary spaces.
            // toLowerCase() supports case-insensitive matching.
            String pattern = "%" + search.trim().toLowerCase() + "%";

            // The search can match ANY of the following fields.
            // Therefore, we use OR.
            return criteriaBuilder.or(

                    // Navigate from Doctor -> User -> firstName.
                    //
                    // root.get("user") gets the associated User entity.
                    // .get("firstName") gets User.firstName.
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("user").get("firstName")
                            ),
                            pattern
                    ),

                    // Search in doctor's last name.
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("user").get("lastName")
                            ),
                            pattern
                    ),

                    // Search in doctor's specialization.
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("specialization")
                            ),
                            pattern
                    )
            );
        };
    }


    /**
     * Creates a qualification filter.
     *
     * A doctor can have:
     *
     * primaryQualification
     * Example: MBBS
     *
     * additionalQualification
     * Example: MD
     *
     * The requested qualification can match either field.
     * Therefore, these conditions are connected using OR.
     *
     * Example:
     * hasQualification("MBBS")
     *
     * Conceptually generates:
     *
     * LOWER(primary_qualification) LIKE '%mbbs%'
     * OR LOWER(additional_qualification) LIKE '%mbbs%'
     *
     * Partial matching is used so the filter can still work
     * when qualification data contains additional text.
     *
     * Example:
     * Database value: "MBBS, MD"
     * Search value: "MBBS"
     * Result: Match
     *
     * If qualification is null or blank,
     * no qualification filter is applied.
     */
    public static Specification<Doctor> hasQualification(
            String qualification
    ) {

        // Qualification filter is optional.
        if (qualification == null || qualification.isBlank()) {
            return null;
        }

        return (root, query, criteriaBuilder) -> {

            // Create a case-insensitive partial match pattern.
            //
            // Example:
            // qualification = "MBBS"
            // pattern = "%mbbs%"
            String pattern =
                    "%" + qualification.trim().toLowerCase() + "%";

            // Match primary qualification OR additional qualification.
            return criteriaBuilder.or(

                    // Check whether primaryQualification contains
                    // the requested qualification.
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("primaryQualification")
                            ),
                            pattern
                    ),

                    // Check whether additionalQualification contains
                    // the requested qualification.
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("additionalQualification")
                            ),
                            pattern
                    )
            );
        };
    }
}