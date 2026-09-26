package com.nexcare.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "availability_occurrence_id",
                                "queue_number"
                        }
                )
        }
)
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "availability_occurrence_id",
            nullable = false
    )
    private AvailabilityOccurrence availabilityOccurrence;

    @Column(
            name = "queue_number",
            nullable = false
    )
    private int queueNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private AppointmentStatus status;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;
}