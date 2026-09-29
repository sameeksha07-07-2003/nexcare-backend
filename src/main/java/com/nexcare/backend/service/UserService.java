package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorProfileRequest;
import com.nexcare.backend.dto.LoginRequest;
import com.nexcare.backend.dto.PatientProfileRequest;
import com.nexcare.backend.dto.SignupRequest;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.entity.Role;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.entity.UserStatus;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.exception.EmailAlreadyExistsException;
import com.nexcare.backend.exception.InvalidCredentialsException;
import com.nexcare.backend.exception.InvalidSignupRequestException;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.PatientRepository;
import com.nexcare.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    private void validateRoleAndProfile(SignupRequest request) {

        if (request.getRole() == null) {
            throw new InvalidSignupRequestException(
                    "Role is required"
            );
        }

        if (request.getRole() == Role.PATIENT) {

            if (request.getPatientProfile() == null) {
                throw new InvalidSignupRequestException(
                        "Patient profile is required for PATIENT role"
                );
            }

            if (request.getDoctorProfile() != null) {
                throw new InvalidSignupRequestException(
                        "Doctor profile must not be provided for PATIENT role"
                );
            }

        } else if (request.getRole() == Role.DOCTOR) {

            if (request.getDoctorProfile() == null) {
                throw new InvalidSignupRequestException(
                        "Doctor profile is required for DOCTOR role"
                );
            }

            if (request.getPatientProfile() != null) {
                throw new InvalidSignupRequestException(
                        "Patient profile must not be provided for DOCTOR role"
                );
            }

        } else {
            throw new InvalidSignupRequestException(
                    "Invalid role for public signup"
            );
        }
    }

    @Transactional
    public User registerUser(SignupRequest request) {

        // 1. Validate role and role-specific profile
        validateRoleAndProfile(request);

        // 2. Check whether email is already registered
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        // 3. Create User entity
        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());

        // Every successfully registered account starts as ACTIVE.
        // Doctor's professional verification is handled separately.
        user.setStatus(UserStatus.ACTIVE);

        // 4. Hash password before storing it
        String hashedPassword = passwordEncoder.encode(
                request.getPassword()
        );

        user.setPasswordHash(hashedPassword);

        // 5. Persist User
        User savedUser = userRepository.save(user);

        // 6. Create role-specific profile
        if (request.getRole() == Role.PATIENT) {

            createPatientProfile(
                    savedUser,
                    request.getPatientProfile()
            );

        } else if (request.getRole() == Role.DOCTOR) {

            createDoctorProfile(
                    savedUser,
                    request.getDoctorProfile()
            );
        }

        return savedUser;
    }

    private void createPatientProfile(
            User user,
            PatientProfileRequest profile
    ) {

        Patient patient = new Patient();

        patient.setUser(user);
        patient.setGender(profile.getGender());
        patient.setDateOfBirth(profile.getDateOfBirth());
        patient.setBloodGroup(profile.getBloodGroup());
        patient.setAddress(profile.getAddress());
        patient.setEmergencyContact(profile.getEmergencyContact());
        patient.setHeight(profile.getHeight());
        patient.setWeight(profile.getWeight());

        patientRepository.save(patient);
    }

    private void createDoctorProfile(
            User user,
            DoctorProfileRequest profile
    ) {

        Doctor doctor = new Doctor();

        doctor.setUser(user);
        doctor.setMedicalRegistrationNumber(
                profile.getMedicalRegistrationNumber()
        );
        doctor.setMedicalCouncil(
                profile.getMedicalCouncil()
        );
        doctor.setRegistrationDate(
                profile.getRegistrationDate()
        );
        doctor.setPrimaryQualification(
                profile.getPrimaryQualification()
        );
        doctor.setAdditionalQualification(
                profile.getAdditionalQualification()
        );
        doctor.setSpecialization(
                profile.getSpecialization()
        );
        doctor.setYearOfPassing(
                profile.getYearOfPassing()
        );
        doctor.setPlaceOfWork(
                profile.getPlaceOfWork()
        );
        doctor.setCity(profile.getCity());

        doctor.setYearsOfExperience(
                profile.getYearsOfExperience()
        );

        doctor.setBio(profile.getBio());

        doctor.setConsultationFee(
                profile.getConsultationFee()
        );

        // This is controlled by the backend.
        // Client cannot decide whether a doctor is verified.
        doctor.setVerificationStatus(
                VerificationStatus.PENDING
        );

        doctorRepository.save(doctor);
    }

    // Login User
    public String loginUser(LoginRequest request) {

        // 1. Find user by email
        Optional<User> dbUser =
                userRepository.findByEmail(
                        request.getEmail()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                );

        // 2. Reject if user does not exist
        if (dbUser.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        // 3. Get existing user
        User existingUser = dbUser.get();

        if (existingUser.getStatus() != UserStatus.ACTIVE
                || existingUser.getDeletedAt() != null) {
            throw new InvalidCredentialsException();
        }

        // 4. Verify password
        if (passwordEncoder.matches(
                request.getPassword(),
                existingUser.getPasswordHash()
        )) {

            // 5. Generate JWT
            return jwtService.generateToken(existingUser);
        }

        // 6. Invalid password
        throw new InvalidCredentialsException();
    }
}

