package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability_occurrence",
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {"doctor_availability_id","appointment_date"}
            )
        }
)
@Getter
public class AvailabilityOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long occurrenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_availability_id",nullable = false)
    @Setter
    private DoctorAvailability  doctorAvailability;

    @Column(name = "appointment_date", nullable = false)
    @Setter
    private LocalDate appointmentDate;

    @Column(name = "booked_patients", nullable = false)
    @Setter
    private int bookedPatients = 0;

    @Setter
    @Column(name = "last_queue_number", nullable = false)
    private int lastQueueNumber = 0;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
