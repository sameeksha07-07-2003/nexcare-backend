package com.nexcare.backend.service;

import com.nexcare.backend.dto.CloudinaryUploadResultRequest;
import com.nexcare.backend.dto.DoctorProfileResponse;
import com.nexcare.backend.dto.DoctorProfileUpdateRequest;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DoctorProfileService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public DoctorProfileService(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public DoctorProfileResponse getProfile(
            String email
    ) {
        User user = findUserByEmail(email);
        Doctor doctor = findDoctorByUser(user);

        return toProfileResponse(user, doctor);
    }

    @Transactional
    public DoctorProfileResponse updateProfile(
            String email,
            DoctorProfileUpdateRequest request
    ) {
        User user = findUserByEmail(email);
        Doctor doctor = findDoctorByUser(user);

        user.setPhoneNumber(
                normalizeRequiredText(
                        request.getPhoneNumber()
                )
        );

        doctor.setPrimaryQualification(
                normalizeRequiredText(
                        request.getPrimaryQualification()
                )
        );

        doctor.setAdditionalQualification(
                normalizeOptionalText(
                        request.getAdditionalQualification()
                )
        );

        doctor.setSpecialization(
                normalizeRequiredText(
                        request.getSpecialization()
                )
        );

        doctor.setYearOfPassing(
                request.getYearOfPassing()
        );

        doctor.setPlaceOfWork(
                normalizeRequiredText(
                        request.getPlaceOfWork()
                )
        );

        doctor.setCity(
                normalizeRequiredText(
                        request.getCity()
                )
        );

        doctor.setYearsOfExperience(
                request.getYearsOfExperience()
        );

        doctor.setBio(
                normalizeOptionalText(
                        request.getBio()
                )
        );

        doctor.setConsultationFee(
                request.getConsultationFee()
        );

        userRepository.save(user);

        Doctor savedDoctor =
                doctorRepository.save(doctor);

        return toProfileResponse(
                user,
                savedDoctor
        );
    }

    @Transactional
    public Map<String, Object>
    generatePhotoUploadSignature(
            String doctorEmail
    ) {
        User user =
                findUserByEmail(doctorEmail);

        Doctor doctor =
                findDoctorByUser(user);

        return cloudinaryService
                .generateDoctorPhotoUploadSignature(
                        doctor.getDoctorId()
                );
    }

    @Transactional
    public DoctorProfileResponse updateProfilePhoto(
            String doctorEmail,
            CloudinaryUploadResultRequest request
    ) {
        User user =
                findUserByEmail(doctorEmail);

        Doctor doctor =
                findDoctorByUser(user);

        String expectedPublicId =
                cloudinaryService
                        .buildDoctorPhotoPublicId(
                                doctor.getDoctorId()
                        );

        /*
         * This verifies:
         * 1. Cloudinary signed the upload response.
         * 2. The public ID belongs to this logged-in doctor.
         * 3. The URL uses HTTPS.
         * 4. The image is hosted by the configured Cloudinary account.
         */
        cloudinaryService.verifyProfilePhotoUpload(
                request.getSecureUrl(),
                request.getPublicId(),
                request.getVersion(),
                request.getSignature(),
                expectedPublicId
        );

        doctor.setProfileImageUrl(
                request.getSecureUrl()
        );

        doctor.setProfileImagePublicId(
                request.getPublicId()
        );

        doctor.setProfileImageVersion(
                request.getVersion()
        );

        Doctor savedDoctor =
                doctorRepository.save(doctor);

        return toProfileResponse(
                user,
                savedDoctor
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

    private Doctor findDoctorByUser(
            User user
    ) {
        return doctorRepository.findByUser(user)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Doctor profile not found."
                        )
                );
    }

    private DoctorProfileResponse toProfileResponse(
            User user,
            Doctor doctor
    ) {
        return DoctorProfileResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .medicalRegistrationNumber(
                        doctor.getMedicalRegistrationNumber()
                )
                .medicalCouncil(
                        doctor.getMedicalCouncil()
                )
                .registrationDate(
                        doctor.getRegistrationDate()
                )
                .primaryQualification(
                        doctor.getPrimaryQualification()
                )
                .additionalQualification(
                        doctor.getAdditionalQualification()
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .yearOfPassing(
                        doctor.getYearOfPassing()
                )
                .placeOfWork(
                        doctor.getPlaceOfWork()
                )
                .city(
                        doctor.getCity()
                )
                .yearsOfExperience(
                        doctor.getYearsOfExperience()
                )
                .bio(
                        doctor.getBio()
                )
                .consultationFee(
                        doctor.getConsultationFee()
                )
                .profileImageUrl(
                        doctor.getProfileImageUrl()
                )
                .averageRating(
                        doctor.getAverageRating()
                )
                .reviewCount(
                        doctor.getReviewCount()
                )
                .verificationStatus(
                        doctor.getVerificationStatus()
                )
                .build();
    }

    private String normalizeRequiredText(
            String value
    ) {
        return value.trim();
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
