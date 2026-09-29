package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Appointment;
import com.nexcare.backend.entity.AppointmentStatus;
import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import com.nexcare.backend.entity.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    boolean existsByPatientAndAvailabilityOccurrenceAndStatusNot(
            Patient patient,
            AvailabilityOccurrence availabilityOccurrence,
            AppointmentStatus excludedStatus
    );

    boolean existsByAvailabilityOccurrenceDoctorAvailability(
            DoctorAvailability doctorAvailability
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.appointmentId = :appointmentId
            """)
    Optional<Appointment> findByIdForUpdate(
            @Param("appointmentId") Long appointmentId
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.patient = :patient
              AND (
                    :status IS NULL
                    OR appointment.status = :status
              )
            ORDER BY
                appointment.availabilityOccurrence.appointmentDate DESC,
                appointment.createdAt DESC
            """)
    Page<Appointment> findPatientAppointments(
            @Param("patient") Patient patient,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.patient = :patient
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND (
                    appointment.availabilityOccurrence.appointmentDate >
                        :currentDate
                    OR (
                        appointment.availabilityOccurrence.appointmentDate =
                            :currentDate
                        AND appointment.availabilityOccurrence
                            .doctorAvailability.endTime > :currentTime
                    )
              )
            ORDER BY
                appointment.availabilityOccurrence.appointmentDate ASC,
                appointment.availabilityOccurrence
                    .doctorAvailability.startTime ASC
            """)
    Page<Appointment> findPatientUpcomingAppointments(
            @Param("patient") Patient patient,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND (
                    :status IS NULL
                    OR appointment.status = :status
              )
            ORDER BY
                appointment.availabilityOccurrence.appointmentDate DESC,
                appointment.queueNumber ASC
            """)
    Page<Appointment> findDoctorAppointments(
            @Param("doctor") Doctor doctor,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND appointment.availabilityOccurrence.appointmentDate =
                    :currentDate
              AND appointment.availabilityOccurrence
                    .doctorAvailability.endTime > :currentTime
            ORDER BY
                appointment.availabilityOccurrence
                    .doctorAvailability.startTime ASC,
                appointment.queueNumber ASC
            """)
    Page<Appointment> findDoctorTodayAppointments(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND appointment.availabilityOccurrence.appointmentDate >
                    :currentDate
            ORDER BY
                appointment.availabilityOccurrence.appointmentDate ASC,
                appointment.availabilityOccurrence
                    .doctorAvailability.startTime ASC,
                appointment.queueNumber ASC
            """)
    Page<Appointment> findDoctorUpcomingAppointments(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user",
            "availabilityOccurrence",
            "availabilityOccurrence.doctorAvailability",
            "availabilityOccurrence.doctorAvailability.doctor",
            "availabilityOccurrence.doctorAvailability.doctor.user"
    })
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND (
                    appointment.availabilityOccurrence.appointmentDate <
                        :currentDate
                    OR (
                        appointment.availabilityOccurrence.appointmentDate =
                            :currentDate
                        AND appointment.availabilityOccurrence
                            .doctorAvailability.endTime <= :currentTime
                    )
              )
            ORDER BY
                appointment.availabilityOccurrence.appointmentDate DESC,
                appointment.availabilityOccurrence
                    .doctorAvailability.startTime DESC,
                appointment.queueNumber ASC
            """)
    Page<Appointment> findDoctorAppointmentsNeedingAction(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(appointment)
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND appointment.availabilityOccurrence.appointmentDate =
                    :currentDate
              AND appointment.availabilityOccurrence
                    .doctorAvailability.endTime > :currentTime
            """)
    long countDoctorTodayAppointments(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

    @Query("""
            SELECT COUNT(appointment)
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND appointment.availabilityOccurrence.appointmentDate >
                    :currentDate
            """)
    long countDoctorUpcomingAppointments(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("""
            SELECT COUNT(appointment)
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status =
                    com.nexcare.backend.entity.AppointmentStatus.BOOKED
              AND (
                    appointment.availabilityOccurrence.appointmentDate <
                        :currentDate
                    OR (
                        appointment.availabilityOccurrence.appointmentDate =
                            :currentDate
                        AND appointment.availabilityOccurrence
                            .doctorAvailability.endTime <= :currentTime
                    )
              )
            """)
    long countDoctorAppointmentsNeedingAction(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

    @Query("""
            SELECT COUNT(appointment)
            FROM Appointment appointment
            WHERE appointment.availabilityOccurrence
                    .doctorAvailability.doctor = :doctor
              AND appointment.status = :status
            """)
    long countDoctorAppointmentsByStatus(
            @Param("doctor") Doctor doctor,
            @Param("status") AppointmentStatus status
    );
}
