package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.entity.VerificationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long>,
        JpaSpecificationExecutor<Doctor> {

    /*
     * Used by doctor profile operations after the authenticated
     * User has already been loaded.
     */
    Optional<Doctor> findByUser(User user);

    /*
     * Used when the authenticated identity is available as an email.
     *
     * Spring Data navigates:
     * Doctor -> User -> email
     */
    Optional<Doctor> findByUserEmail(String email);

    /*
     * Public doctor discovery and profile endpoints must only return
     * APPROVED doctors.
     */
    Optional<Doctor> findByDoctorIdAndVerificationStatus(
            Long doctorId,
            VerificationStatus verificationStatus
    );

    /*
     * Locks the doctor row while changing denormalized rating fields:
     * averageRating and reviewCount.
     *
     * This prevents two simultaneous reviews from overwriting each
     * other's rating calculations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT doctor
            FROM Doctor doctor
            WHERE doctor.doctorId = :doctorId
            """)
    Optional<Doctor> findByIdForUpdate(
            @Param("doctorId")
            Long doctorId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT doctor
            FROM Doctor doctor
            JOIN FETCH doctor.user
            WHERE doctor.doctorId = :doctorId
            """)
    Optional<Doctor> findByIdForVerificationUpdate(
            @Param("doctorId")
            Long doctorId
    );

    long countByVerificationStatus(
            VerificationStatus verificationStatus
    );

    /*
     * Values used to populate the public city filter.
     */
    @Query("""
            SELECT DISTINCT doctor.city
            FROM Doctor doctor
            WHERE doctor.verificationStatus = :status
              AND doctor.city IS NOT NULL
              AND TRIM(doctor.city) <> ''
            ORDER BY doctor.city
            """)
    List<String> findDistinctCitiesByVerificationStatus(
            @Param("status")
            VerificationStatus status
    );

    /*
     * Values used to populate the public specialization filter.
     */
    @Query("""
            SELECT DISTINCT doctor.specialization
            FROM Doctor doctor
            WHERE doctor.verificationStatus = :status
              AND doctor.specialization IS NOT NULL
              AND TRIM(doctor.specialization) <> ''
            ORDER BY doctor.specialization
            """)
    List<String>
    findDistinctSpecializationsByVerificationStatus(
            @Param("status")
            VerificationStatus status
    );

    /*
     * Primary qualifications such as MBBS or MD Medicine.
     */
    @Query("""
            SELECT DISTINCT doctor.primaryQualification
            FROM Doctor doctor
            WHERE doctor.verificationStatus = :status
              AND doctor.primaryQualification IS NOT NULL
              AND TRIM(doctor.primaryQualification) <> ''
            ORDER BY doctor.primaryQualification
            """)
    List<String>
    findDistinctPrimaryQualificationsByVerificationStatus(
            @Param("status")
            VerificationStatus status
    );

    /*
     * Additional qualifications such as DM Cardiology.
     */
    @Query("""
            SELECT DISTINCT doctor.additionalQualification
            FROM Doctor doctor
            WHERE doctor.verificationStatus = :status
              AND doctor.additionalQualification IS NOT NULL
              AND TRIM(doctor.additionalQualification) <> ''
            ORDER BY doctor.additionalQualification
            """)
    List<String>
    findDistinctAdditionalQualificationsByVerificationStatus(
            @Param("status")
            VerificationStatus status
    );
}
