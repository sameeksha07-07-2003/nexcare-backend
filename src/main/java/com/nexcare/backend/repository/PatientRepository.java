package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);
}
