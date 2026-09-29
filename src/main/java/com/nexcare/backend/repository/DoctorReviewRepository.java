package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Appointment;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorReviewRepository
        extends JpaRepository<DoctorReview, Long> {

    boolean existsByAppointment(Appointment appointment);

    @EntityGraph(attributePaths = {
            "patient",
            "patient.user"
    })
    Page<DoctorReview> findByDoctorOrderByCreatedAtDesc(
            Doctor doctor,
            Pageable pageable
    );
}