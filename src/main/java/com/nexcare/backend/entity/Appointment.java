package com.nexcare.backend.entity;

import jakarta.persistence.*;
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
                        name = "uk_appointment_occurrence_queue",
                        columnNames = {
                                "availability_occurrence_id",
                                "queue_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_appointment_patient_status",
                        columnList = "patient_id, status"
                ),
                @Index(
                        name = "idx_appointment_occurrence_status",
                        columnList = "availability_occurrence_id, status"
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AppointmentStatus status;

    @Column(
            name = "reason_for_visit",
            length = 500
    )
    private String reasonForVisit;

    @Column(
            name = "cancellation_reason",
            length = 500
    )
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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