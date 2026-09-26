package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Appointment;
import com.nexcare.backend.entity.AppointmentStatus;
import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByPatientAndAvailabilityOccurrenceAndStatusNot(
            Patient patient,
            AvailabilityOccurrence availabilityOccurrence,
            AppointmentStatus status
    );
}