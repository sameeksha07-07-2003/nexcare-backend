package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorProfileResponse;
import com.nexcare.backend.dto.DoctorProfileUpdateRequest;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DoctorProfileService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DoctorProfileService(
            DoctorRepository doctorRepository,
            UserRepository userRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public DoctorProfileResponse getProfile(String email) {

        // Step 1: Find the authenticated User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Step 2: Find the Doctor profile associated with this User
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found"));

        // Step 3: Convert entity data into a response DTO
        return toProfileResponse(user, doctor);
    }

    @Transactional
    public DoctorProfileResponse updateProfile(
            String email,
            DoctorProfileUpdateRequest request
    ) {

        // Step 1: Find the authenticated User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Step 2: Find the Doctor profile associated with this User
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found"));

        // Step 3: Update User fields
        user.setPhoneNumber(request.getPhoneNumber());

        // Step 4: Update Doctor fields
        doctor.setPrimaryQualification(
                request.getPrimaryQualification()
        );
        doctor.setAdditionalQualification(
                request.getAdditionalQualification()
        );
        doctor.setSpecialization(
                request.getSpecialization()
        );
        doctor.setYearOfPassing(
                request.getYearOfPassing()
        );
        doctor.setPlaceOfWork(
                request.getPlaceOfWork()
        );

        // Step 5: Persist changes
        userRepository.save(user);
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Step 6: Return updated profile
        return toProfileResponse(user, savedDoctor);
    }

    private DoctorProfileResponse toProfileResponse(
            User user,
            Doctor doctor
    ) {
        return new DoctorProfileResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                doctor.getAdditionalQualification(),
                doctor.getPlaceOfWork(),
                doctor.getSpecialization(),
                doctor.getMedicalCouncil(),
                doctor.getMedicalRegistrationNumber(),
                doctor.getPrimaryQualification(),
                doctor.getVerificationStatus(),
                doctor.getYearOfPassing()
        );
    }
}