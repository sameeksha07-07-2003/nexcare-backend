package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    Optional<Doctor> findByUser(User user);
}
