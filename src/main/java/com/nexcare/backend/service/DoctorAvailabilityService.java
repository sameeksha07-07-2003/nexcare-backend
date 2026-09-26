package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorAvailabilityRequest;
import com.nexcare.backend.dto.DoctorAvailabilityResponse;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import com.nexcare.backend.entity.User;
import com.nexcare.backend.repository.DoctorAvailabilityRepository;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DoctorAvailabilityService(
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            UserRepository userRepository,
            DoctorRepository doctorRepository) {

        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    public DoctorAvailabilityResponse createAvailability(
            DoctorAvailabilityRequest doctorAvailabilityRequest) {

        // 1. Validate time range
        if (!doctorAvailabilityRequest.getEndTime()
                .isAfter(doctorAvailabilityRequest.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        // 2. Get authenticated user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        // 3. Find User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 4. Find Doctor associated with this User
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        // 5. Check whether an overlapping availability already exists
        boolean overlapExists =
                doctorAvailabilityRepository.existsOverlappingAvailability(
                        doctor,
                        doctorAvailabilityRequest.getDayOfWeek(),
                        doctorAvailabilityRequest.getStartTime(),
                        doctorAvailabilityRequest.getEndTime()
                );

        // 6. Reject if overlap exists
        if (overlapExists) {
            throw new IllegalArgumentException(
                    "Availability overlaps with an existing time window"
            );
        }

        // 7. Create DoctorAvailability entity
        DoctorAvailability availability = new DoctorAvailability(
                doctorAvailabilityRequest.getDayOfWeek(),
                doctorAvailabilityRequest.getStartTime(),
                doctorAvailabilityRequest.getEndTime(),
                doctorAvailabilityRequest.getMaxPatientsAllowed(),
                doctor
        );

        // 8. Save availability
        DoctorAvailability savedAvailability =
                doctorAvailabilityRepository.save(availability);

        // 9. Convert Entity → Response DTO
        return new DoctorAvailabilityResponse(
                savedAvailability.getId(),
                savedAvailability.getDayOfWeek(),
                savedAvailability.getStartTime(),
                savedAvailability.getEndTime(),
                savedAvailability.getMaxPatientsAllowed()
        );
    }
}