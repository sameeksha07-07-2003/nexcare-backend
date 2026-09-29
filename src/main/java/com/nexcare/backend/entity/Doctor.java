package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_user",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_doctor_registration_number",
                        columnNames = "medical_registration_number"
                )
        },
        indexes = {
                @Index(
                        name = "idx_doctor_verification_city",
                        columnList = "verification_status, city"
                ),
                @Index(
                        name = "idx_doctor_specialization",
                        columnList = "specialization"
                )
        }
)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Setter
    @Column(
            name = "medical_registration_number",
            nullable = false,
            length = 100
    )
    private String medicalRegistrationNumber;

    @Setter
    @Column(
            name = "medical_council",
            nullable = false,
            length = 150
    )
    private String medicalCouncil;

    @Setter
    @Column(
            name = "registration_date",
            nullable = false
    )
    private LocalDate registrationDate;

    @Setter
    @Column(
            name = "primary_qualification",
            nullable = false,
            length = 255
    )
    private String primaryQualification;

    @Setter
    @Column(
            name = "additional_qualification",
            length = 255
    )
    private String additionalQualification;

    @Setter
    @Column(
            name = "specialization",
            length = 150
    )
    private String specialization;

    @Setter
    @Column(name = "year_of_passing")
    private Integer yearOfPassing;

    @Setter
    @Column(
            name = "place_of_work",
            length = 255
    )
    private String placeOfWork;

    @Setter
    @Column(
            name = "city",
            length = 100
    )
    private String city;

    @Setter
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Setter
    @Column(name = "bio", length = 2000)
    private String bio;

    @Setter
    @Column(
            name = "consultation_fee",
            precision = 10,
            scale = 2
    )
    private BigDecimal consultationFee;

    @Setter
    @Column(
            name = "profile_image_url",
            length = 1000
    )
    private String profileImageUrl;

    @Setter
    @Column(
            name = "profile_image_public_id",
            length = 255
    )
    private String profileImagePublicId;

    @Setter
    @Column(name = "profile_image_version")
    private Long profileImageVersion;

    /*
     * These fields are denormalized intentionally.
     *
     * Doctor Discovery reads these values frequently. Storing the summary
     * prevents an expensive reviews aggregation query for every doctor card.
     */
    @Setter
    @Column(
            name = "average_rating",
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Setter
    @Column(
            name = "review_count",
            nullable = false
    )
    private Integer reviewCount = 0;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "verification_status",
            nullable = false,
            length = 30
    )
    private VerificationStatus verificationStatus;

    @Setter
    @Column(
            name = "verification_reason",
            length = 1000
    )
    private String verificationReason;

    @Setter
    @Column(name = "verification_reviewed_at")
    private LocalDateTime verificationReviewedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "verification_reviewed_by_user_id",
            foreignKey = @ForeignKey(
                    name = "fk_doctor_verification_reviewer"
            )
    )
    private User verificationReviewedBy;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}
