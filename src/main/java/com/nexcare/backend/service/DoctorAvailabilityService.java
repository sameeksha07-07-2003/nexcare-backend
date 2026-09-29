package com.nexcare.backend.service;

import com.nexcare.backend.dto.DoctorAvailabilityRequest;
import com.nexcare.backend.dto.DoctorAvailabilityResponse;
import com.nexcare.backend.dto.PublicDoctorAvailabilityResponse;
import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.repository.AvailabilityOccurrenceRepository;
import com.nexcare.backend.repository.AppointmentRepository;
import com.nexcare.backend.repository.DoctorAvailabilityRepository;
import com.nexcare.backend.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorAvailabilityService {

    private static final int MAXIMUM_PUBLIC_SCHEDULE_DAYS = 21;

    private static final ZoneId BUSINESS_TIME_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final DoctorAvailabilityRepository
            doctorAvailabilityRepository;

    private final AvailabilityOccurrenceRepository
            availabilityOccurrenceRepository;

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityService(
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            AvailabilityOccurrenceRepository
                    availabilityOccurrenceRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.doctorAvailabilityRepository =
                doctorAvailabilityRepository;

        this.availabilityOccurrenceRepository =
                availabilityOccurrenceRepository;

        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public DoctorAvailabilityResponse createAvailability(
            String doctorEmail,
            DoctorAvailabilityRequest request
    ) {
        validateRequest(request);

        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        boolean overlapExists =
                doctorAvailabilityRepository
                        .existsOverlappingAvailability(
                                doctor,
                                request.getDayOfWeek(),
                                request.getStartTime(),
                                request.getEndTime()
                        );

        if (overlapExists) {
            throw new IllegalArgumentException(
                    "Availability overlaps with an existing time window."
            );
        }

        DoctorAvailability availability =
                new DoctorAvailability(
                        request.getDayOfWeek(),
                        request.getStartTime(),
                        request.getEndTime(),
                        request.getMaxPatientsAllowed(),
                        doctor
                );

        DoctorAvailability savedAvailability =
                doctorAvailabilityRepository.save(availability);

        return toResponse(savedAvailability);
    }

    @Transactional
    public List<DoctorAvailabilityResponse> getMyAvailabilities(
            String doctorEmail
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        return doctorAvailabilityRepository
                .findByDoctorOrderByDayOfWeekAscStartTimeAsc(
                        doctor
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(
            String doctorEmail,
            Long availabilityId,
            DoctorAvailabilityRequest request
    ) {
        validateRequest(request);

        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndDoctor(
                                availabilityId,
                                doctor
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor availability not found."
                                )
                        );

        if (!availability.isActive()) {
            throw new IllegalStateException(
                    "Inactive availability cannot be updated."
            );
        }

        if (appointmentRepository
                .existsByAvailabilityOccurrenceDoctorAvailability(
                        availability
                )) {
            throw new IllegalStateException(
                    "Availability with appointment history cannot be edited. "
                            + "Deactivate it and create a new schedule instead."
            );
        }

        boolean overlapExists =
                doctorAvailabilityRepository
                        .existsOverlappingAvailabilityExcludingId(
                                doctor,
                                availabilityId,
                                request.getDayOfWeek(),
                                request.getStartTime(),
                                request.getEndTime()
                        );

        if (overlapExists) {
            throw new IllegalArgumentException(
                    "Availability overlaps with an existing time window."
            );
        }

        availability.setDayOfWeek(
                request.getDayOfWeek()
        );

        availability.setStartTime(
                request.getStartTime()
        );

        availability.setEndTime(
                request.getEndTime()
        );

        availability.setMaxPatientsAllowed(
                request.getMaxPatientsAllowed()
        );

        DoctorAvailability savedAvailability =
                doctorAvailabilityRepository.save(availability);

        return toResponse(savedAvailability);
    }

    @Transactional
    public void deactivateAvailability(
            String doctorEmail,
            Long availabilityId
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndDoctor(
                                availabilityId,
                                doctor
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor availability not found."
                                )
                        );

        if (!availability.isActive()) {
            return;
        }

        /*
         * Existing appointments remain safe because the availability
         * is not permanently deleted.
         */
        availability.setActive(false);

        doctorAvailabilityRepository.save(availability);
    }

    @Transactional
    public DoctorAvailabilityResponse reactivateAvailability(
            String doctorEmail,
            Long availabilityId
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        DoctorAvailability availability =
                doctorAvailabilityRepository
                        .findByIdAndDoctor(
                                availabilityId,
                                doctor
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor availability not found."
                                )
                        );

        if (availability.isActive()) {
            return toResponse(availability);
        }

        boolean overlapExists =
                doctorAvailabilityRepository
                        .existsOverlappingAvailabilityExcludingId(
                                doctor,
                                availabilityId,
                                availability.getDayOfWeek(),
                                availability.getStartTime(),
                                availability.getEndTime()
                        );

        if (overlapExists) {
            throw new IllegalArgumentException(
                    "This availability now overlaps with another active window."
            );
        }

        availability.setActive(true);

        DoctorAvailability savedAvailability =
                doctorAvailabilityRepository.save(availability);

        return toResponse(savedAvailability);
    }

    @Transactional
    public List<PublicDoctorAvailabilityResponse>
    getPublicAvailability(
            Long doctorId,
            LocalDate startDate,
            int days
    ) {
        if (days < 1 || days > MAXIMUM_PUBLIC_SCHEDULE_DAYS) {
            throw new IllegalArgumentException(
                    "Days must be between 1 and 21."
            );
        }

        LocalDateTime currentDateTime =
                LocalDateTime.now(BUSINESS_TIME_ZONE);

        LocalDate currentDate = currentDateTime.toLocalDate();

        LocalDate effectiveStartDate =
                startDate == null ? currentDate : startDate;

        if (effectiveStartDate.isBefore(currentDate)) {
            throw new IllegalArgumentException(
                    "Availability start date cannot be in the past."
            );
        }

        LocalDate endDate =
                effectiveStartDate.plusDays(days - 1L);

        Doctor doctor = doctorRepository
                .findByDoctorIdAndVerificationStatus(
                        doctorId,
                        VerificationStatus.APPROVED
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Approved doctor not found."
                        )
                );

        List<DoctorAvailability> availabilities =
                doctorAvailabilityRepository
                        .findByDoctorAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                                doctor
                        );

        if (availabilities.isEmpty()) {
            return List.of();
        }

        List<AvailabilityOccurrence> occurrences =
                availabilityOccurrenceRepository
                        .findByDoctorAvailabilityInAndAppointmentDateBetween(
                                availabilities,
                                effectiveStartDate,
                                endDate
                        );

        Map<String, AvailabilityOccurrence> occurrenceMap =
                createOccurrenceMap(occurrences);

        return effectiveStartDate
                .datesUntil(endDate.plusDays(1))
                .flatMap(date ->
                        availabilities.stream()
                                .filter(availability ->
                                        availability.getDayOfWeek()
                                                == date.getDayOfWeek()
                                )
                                .filter(availability ->
                                        !date.equals(currentDate)
                                                || availability.getStartTime()
                                                .isAfter(currentDateTime.toLocalTime())
                                )
                                .sorted(
                                        Comparator.comparing(
                                                DoctorAvailability::getStartTime
                                        )
                                )
                                .map(availability ->
                                        toPublicResponse(
                                                availability,
                                                date,
                                                occurrenceMap
                                        )
                                )
                )
                .toList();
    }

    private PublicDoctorAvailabilityResponse toPublicResponse(
            DoctorAvailability availability,
            LocalDate appointmentDate,
            Map<String, AvailabilityOccurrence> occurrenceMap
    ) {
        String key = createOccurrenceKey(
                availability.getId(),
                appointmentDate
        );

        AvailabilityOccurrence occurrence =
                occurrenceMap.get(key);

        int bookedPatients =
                occurrence == null
                        ? 0
                        : occurrence.getBookedPatients();

        int remainingCapacity = Math.max(
                availability.getMaxPatientsAllowed()
                        - bookedPatients,
                0
        );

        return PublicDoctorAvailabilityResponse.builder()
                .doctorAvailabilityId(
                        availability.getId()
                )
                .appointmentDate(appointmentDate)
                .dayOfWeek(
                        availability.getDayOfWeek()
                )
                .startTime(
                        availability.getStartTime()
                )
                .endTime(
                        availability.getEndTime()
                )
                .maxPatients(
                        availability.getMaxPatientsAllowed()
                )
                .bookedPatients(bookedPatients)
                .remainingCapacity(remainingCapacity)
                .fullyBooked(remainingCapacity == 0)
                .build();
    }

    private Map<String, AvailabilityOccurrence>
    createOccurrenceMap(
            List<AvailabilityOccurrence> occurrences
    ) {
        Map<String, AvailabilityOccurrence> result =
                new HashMap<>();

        for (AvailabilityOccurrence occurrence : occurrences) {
            String key = createOccurrenceKey(
                    occurrence.getDoctorAvailability().getId(),
                    occurrence.getAppointmentDate()
            );

            result.put(key, occurrence);
        }

        return result;
    }

    private String createOccurrenceKey(
            Long availabilityId,
            LocalDate appointmentDate
    ) {
        return availabilityId + "_" + appointmentDate;
    }

    private Doctor findDoctorByEmail(String email) {
        return doctorRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Doctor profile not found."
                        )
                );
    }

    private void validateRequest(
            DoctorAvailabilityRequest request
    ) {
        if (!request.getEndTime()
                .isAfter(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }
    }

    private DoctorAvailabilityResponse toResponse(
            DoctorAvailability availability
    ) {
        return DoctorAvailabilityResponse.builder()
                .id(availability.getId())
                .dayOfWeek(
                        availability.getDayOfWeek()
                )
                .startTime(
                        availability.getStartTime()
                )
                .endTime(
                        availability.getEndTime()
                )
                .maxPatientsAllowed(
                        availability.getMaxPatientsAllowed()
                )
                .active(availability.isActive())
                .build();
    }
}
