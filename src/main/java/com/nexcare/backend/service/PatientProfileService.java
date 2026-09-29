package com.nexcare.backend.service;

import com.nexcare.backend.dto.CloudinaryUploadResultRequest;
import com.nexcare.backend.dto.PatientProfileResponse;
import com.nexcare.backend.dto.PatientProfileUpdateRequest;
import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.PatientRepository;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PatientProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final CloudinaryService cloudinaryService;

    public PatientProfileService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            CloudinaryService cloudinaryService
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public PatientProfileResponse getProfile(
            String email
    ) {
        User user = findUserByEmail(email);
        Patient patient = findPatientByUser(user);

        return toProfileResponse(user, patient);
    }

    @Transactional
    public PatientProfileResponse updateProfile(
            String email,
            PatientProfileUpdateRequest request
    ) {
        User user = findUserByEmail(email);
        Patient patient = findPatientByUser(user);

        patient.setGender(
                request.getGender()
        );

        patient.setDateOfBirth(
                request.getDateOfBirth()
        );

        patient.setBloodGroup(
                request.getBloodGroup()
        );

        patient.setAddress(
                normalizeOptionalText(
                        request.getAddress()
                )
        );

        patient.setEmergencyContact(
                normalizeOptionalText(
                        request.getEmergencyContact()
                )
        );

        patient.setHeight(
                request.getHeight()
        );

        patient.setWeight(
                request.getWeight()
        );

        Patient savedPatient =
                patientRepository.save(patient);

        return toProfileResponse(
                user,
                savedPatient
        );
    }

    @Transactional
    public Map<String, Object>
    generatePhotoUploadSignature(
            String patientEmail
    ) {
        User user =
                findUserByEmail(patientEmail);

        Patient patient =
                findPatientByUser(user);

        return cloudinaryService
                .generatePatientPhotoUploadSignature(
                        patient.getPatientId()
                );
    }

    @Transactional
    public PatientProfileResponse updateProfilePhoto(
            String patientEmail,
            CloudinaryUploadResultRequest request
    ) {
        User user =
                findUserByEmail(patientEmail);

        Patient patient =
                findPatientByUser(user);

        String expectedPublicId =
                cloudinaryService
                        .buildPatientPhotoPublicId(
                                patient.getPatientId()
                        );

        cloudinaryService.verifyProfilePhotoUpload(
                request.getSecureUrl(),
                request.getPublicId(),
                request.getVersion(),
                request.getSignature(),
                expectedPublicId
        );

        patient.setPhotoUrl(
                request.getSecureUrl()
        );

        patient.setPhotoPublicId(
                request.getPublicId()
        );

        patient.setPhotoVersion(
                request.getVersion()
        );

        Patient savedPatient =
                patientRepository.save(patient);

        return toProfileResponse(
                user,
                savedPatient
        );
    }

    private User findUserByEmail(
            String email
    ) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found."
                        )
                );
    }

    private Patient findPatientByUser(
            User user
    ) {
        return patientRepository.findByUser(user)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Patient profile not found."
                        )
                );
    }

    private PatientProfileResponse toProfileResponse(
            User user,
            Patient patient
    ) {
        return new PatientProfileResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                patient.getGender(),
                patient.getDateOfBirth(),
                patient.getBloodGroup(),
                patient.getAddress(),
                patient.getEmergencyContact(),
                patient.getHeight(),
                patient.getWeight(),
                patient.getPhotoUrl()
        );
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}