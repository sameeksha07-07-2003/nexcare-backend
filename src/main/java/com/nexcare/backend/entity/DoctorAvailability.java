package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private DayOfWeek dayOfWeek;

    @Setter
    @Column(nullable = false)
    private LocalTime startTime;

    @Setter
    @Column(nullable = false)
    private LocalTime endTime;

    @Setter
    @Column(nullable = false)
    private Integer maxPatientsAllowed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    protected DoctorAvailability() {
        // Required by JPA
    }

    public DoctorAvailability(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxPatientsAllowed,
            Doctor doctor) {

        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatientsAllowed = maxPatientsAllowed;
        this.doctor = doctor;
    }
}