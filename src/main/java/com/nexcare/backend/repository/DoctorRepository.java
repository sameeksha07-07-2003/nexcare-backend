package com.nexcare.backend.repository;

import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * Repository responsible for database operations related to Doctor entities.
 *
 * JpaRepository:
 * Provides basic CRUD operations.
 *
 * JpaSpecificationExecutor:
 * Provides support for dynamic queries using Specification<Doctor>.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor,Long> , JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByUser(User user);
}
