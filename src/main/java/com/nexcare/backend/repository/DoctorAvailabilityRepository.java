package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability>
    findByDoctorOrderByDayOfWeekAscStartTimeAsc(
            Doctor doctor
    );

    List<DoctorAvailability>
    findByDoctorAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            Doctor doctor
    );

    List<DoctorAvailability>
    findByDoctorDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            Long doctorId
    );

    Optional<DoctorAvailability> findByIdAndDoctor(
            Long availabilityId,
            Doctor doctor
    );

    @Query("""
            SELECT COUNT(availability) > 0
            FROM DoctorAvailability availability
            WHERE availability.doctor = :doctor
              AND availability.active = true
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.startTime < :endTime
              AND availability.endTime > :startTime
            """)
    boolean existsOverlappingAvailability(
            @Param("doctor") Doctor doctor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT COUNT(availability) > 0
            FROM DoctorAvailability availability
            WHERE availability.doctor = :doctor
              AND availability.active = true
              AND availability.id <> :excludedAvailabilityId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.startTime < :endTime
              AND availability.endTime > :startTime
            """)
    boolean existsOverlappingAvailabilityExcludingId(
            @Param("doctor") Doctor doctor,
            @Param("excludedAvailabilityId")
            Long excludedAvailabilityId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}