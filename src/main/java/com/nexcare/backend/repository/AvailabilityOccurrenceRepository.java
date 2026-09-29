package com.nexcare.backend.repository;

import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.DoctorAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityOccurrenceRepository
        extends JpaRepository<AvailabilityOccurrence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AvailabilityOccurrence>
    findByDoctorAvailabilityAndAppointmentDate(
            DoctorAvailability doctorAvailability,
            LocalDate appointmentDate
    );

    List<AvailabilityOccurrence>
    findByDoctorAvailabilityInAndAppointmentDateBetween(
            List<DoctorAvailability> doctorAvailabilities,
            LocalDate startDate,
            LocalDate endDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT occurrence
            FROM AvailabilityOccurrence occurrence
            WHERE occurrence.occurrenceId = :occurrenceId
            """)
    Optional<AvailabilityOccurrence> findByIdForUpdate(
            @Param("occurrenceId") Long occurrenceId
    );
}