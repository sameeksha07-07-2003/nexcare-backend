package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@Table(
        name = "doctor_availability",
        indexes = {
                @Index(
                        name = "idx_availability_doctor_day_active",
                        columnList = "doctor_id, day_of_week, active"
                )
        }
)
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_of_week",
            nullable = false,
            length = 20
    )
    private DayOfWeek dayOfWeek;

    @Setter
    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Setter
    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;

    @Setter
    @Column(
            name = "max_patients_allowed",
            nullable = false
    )
    private Integer maxPatientsAllowed;

    /*
     * We deactivate availability instead of permanently deleting it.
     * Existing appointments may still reference this availability.
     */
    @Setter
    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "doctor_id",
            nullable = false
    )
    private Doctor doctor;

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

    protected DoctorAvailability() {
    }

    public DoctorAvailability(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxPatientsAllowed,
            Doctor doctor
    ) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatientsAllowed = maxPatientsAllowed;
        this.doctor = doctor;
        this.active = true;
    }
}