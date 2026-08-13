package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_user", columnNames = "user_id"),
                @UniqueConstraint(
                        name = "uk_doctor_registration_number",
                        columnNames = "medical_registration_number"
                )
        }
)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Setter
    @OneToOne(optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Setter
    @Column(
            name = "medical_registration_number",
            nullable = false
    )
    private String medicalRegistrationNumber;

    @Setter
    @Column(
            name = "medical_council",
            nullable = false
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
            nullable = false
    )
    private String primaryQualification;

    @Setter
    @Column(name = "additional_qualification")
    private String additionalQualification;

    @Setter
    @Column(name = "specialization")
    private String specialization;

    @Setter
    @Column(name = "year_of_passing")
    private Integer yearOfPassing;

    @Setter
    @Column(name = "place_of_work")
    private String placeOfWork;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "verification_status",
            nullable = false
    )
    private VerificationStatus verificationStatus;

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