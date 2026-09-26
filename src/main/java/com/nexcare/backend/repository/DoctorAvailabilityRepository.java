package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctor(Doctor doctor);

    @Query("""
            SELECT COUNT(a) > 0
            FROM DoctorAvailability a
            WHERE a.doctor = :doctor
              AND a.dayOfWeek = :dayOfWeek
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    boolean existsOverlappingAvailability(
            @Param("doctor") Doctor doctor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
