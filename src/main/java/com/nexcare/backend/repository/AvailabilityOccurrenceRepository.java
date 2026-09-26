package com.nexcare.backend.repository;

import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.DoctorAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.Optional;

public interface AvailabilityOccurrenceRepository
        extends JpaRepository<AvailabilityOccurrence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AvailabilityOccurrence> findByDoctorAvailabilityAndAppointmentDate(
            DoctorAvailability doctorAvailability,
            LocalDate appointmentDate
    );
}