package com.nexcare.backend.service;

import com.nexcare.backend.dto.PatientProfileResponse;
import com.nexcare.backend.dto.PatientProfileUpdateRequest;
import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.PatientRepository;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PatientProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final CloudinaryService cloudinaryService;

    public PatientProfileService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            CloudinaryService cloudinaryService) {

        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /*
     * Get the profile of the currently authenticated patient.
     *
     * The email comes from the authenticated JWT.
     * We use it to find the User and then find the
     * Patient associated with that User.
     */
    public PatientProfileResponse getProfile(String email) {

        // Step 1: Find the User using the authenticated user's email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Step 2: Find the Patient profile associated with this User
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Patient profile not found"));

        // Step 3: Convert entity data into a safe response DTO
        return toProfileResponse(user, patient);
    }

    /*
     * Update the profile of the currently authenticated patient.
     *
     * The email comes from the authenticated JWT.
     * The client does NOT provide a patientId or userId.
     */
    @Transactional
    public PatientProfileResponse updateProfile(
            String email,
            PatientProfileUpdateRequest request) {

        // Step 1: Find the authenticated User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Step 2: Find the Patient belonging to this User
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Patient profile not found"));

        // Step 3: Update the Patient profile
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setHeight(request.getHeight());
        patient.setWeight(request.getWeight());

        // Step 4: Persist the updated Patient
        Patient savedPatient = patientRepository.save(patient);

        // Step 5: Return the updated profile
        return toProfileResponse(user, savedPatient);
    }

    /*
     * Uploads a new profile photo to Cloudinary and saves the resulting
     * URL on the Patient. Kept separate from updateProfile() since it's a
     * multipart request, not a JSON body.
     */
    @Transactional
    public PatientProfileResponse updateProfilePhoto(String email, MultipartFile file) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Patient profile not found"));

        String photoUrl = cloudinaryService.uploadPatientPhoto(file);
        patient.setPhotoUrl(photoUrl);

        Patient savedPatient = patientRepository.save(patient);

        return toProfileResponse(user, savedPatient);
    }

    /*
     * Converts User + Patient entities into the response DTO.
     *
     * Keeping this mapping in one place avoids duplicating
     * the same constructor code in getProfile() and updateProfile().
     */
    private PatientProfileResponse toProfileResponse(
            User user,
            Patient patient) {

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
}