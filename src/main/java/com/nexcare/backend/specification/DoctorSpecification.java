package com.nexcare.backend.specification;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.VerificationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class DoctorSpecification {

    private DoctorSpecification() {
    }

    public static Specification<Doctor>
    hasVerificationStatus(
            VerificationStatus status
    ) {
        return (root, query, builder) ->
                builder.equal(
                        root.get("verificationStatus"),
                        status
                );
    }

    public static Specification<Doctor> hasCity(
            String city
    ) {
        if (city == null || city.isBlank()) {
            return null;
        }

        String normalizedCity =
                city.trim().toLowerCase();

        return (root, query, builder) ->
                builder.equal(
                        builder.lower(
                                root.get("city")
                        ),
                        normalizedCity
                );
    }

    public static Specification<Doctor>
    hasSpecialization(
            String specialization
    ) {
        if (specialization == null
                || specialization.isBlank()) {

            return null;
        }

        String normalizedSpecialization =
                specialization.trim().toLowerCase();

        return (root, query, builder) ->
                builder.equal(
                        builder.lower(
                                root.get("specialization")
                        ),
                        normalizedSpecialization
                );
    }

    public static Specification<Doctor>
    hasMinimumExperience(
            Integer minimumExperience
    ) {
        if (minimumExperience == null) {
            return null;
        }

        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(
                        root.get("yearsOfExperience"),
                        minimumExperience
                );
    }

    public static Specification<Doctor>
    hasQualification(
            String qualification
    ) {
        if (qualification == null
                || qualification.isBlank()) {

            return null;
        }

        String pattern =
                "%"
                        + qualification
                        .trim()
                        .toLowerCase()
                        + "%";

        return (root, query, builder) ->
                builder.or(
                        builder.like(
                                builder.lower(
                                        root.get(
                                                "primaryQualification"
                                        )
                                ),
                                pattern
                        ),
                        builder.like(
                                builder.lower(
                                        root.get(
                                                "additionalQualification"
                                        )
                                ),
                                pattern
                        )
                );
    }

    public static Specification<Doctor>
    matchesSearch(
            String search
    ) {
        if (search == null || search.isBlank()) {
            return null;
        }

        String pattern =
                "%"
                        + search.trim().toLowerCase()
                        + "%";

        return (root, query, builder) -> {
            String normalizedFullName =
                    search.trim()
                            .toLowerCase()
                            .replaceAll("\\s+", " ");

            String fullNamePattern =
                    "%" + normalizedFullName + "%";

            return builder.or(
                    builder.like(
                            builder.lower(
                                    root.get("user")
                                            .get("firstName")
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get("user")
                                            .get("lastName")
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    builder.concat(
                                            builder.concat(
                                                    root.get("user")
                                                            .get("firstName"),
                                                    " "
                                            ),
                                            root.get("user")
                                                    .get("lastName")
                                    )
                            ),
                            fullNamePattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get("specialization")
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get(
                                            "primaryQualification"
                                    )
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get(
                                            "additionalQualification"
                                    )
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get("placeOfWork")
                            ),
                            pattern
                    )
            );
        };
    }
}