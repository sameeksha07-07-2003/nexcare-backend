package com.nexcare.backend.service;

import com.nexcare.backend.dto.AppointmentCancellationRequest;
import com.nexcare.backend.dto.AppointmentRequestDto;
import com.nexcare.backend.dto.AppointmentResponseDto;
import com.nexcare.backend.dto.DoctorAppointmentScope;
import com.nexcare.backend.dto.DoctorAppointmentSummaryDto;
import com.nexcare.backend.entity.Appointment;
import com.nexcare.backend.entity.AppointmentStatus;
import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.Doctor;
import com.nexcare.backend.entity.DoctorAvailability;
import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.entity.VerificationStatus;
import com.nexcare.backend.repository.AppointmentRepository;
import com.nexcare.backend.repository.AvailabilityOccurrenceRepository;
import com.nexcare.backend.repository.DoctorAvailabilityRepository;
import com.nexcare.backend.repository.DoctorRepository;
import com.nexcare.backend.repository.DoctorReviewRepository;
import com.nexcare.backend.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final long
            MAXIMUM_ADVANCE_BOOKING_WEEKS = 3;

    private static final int MAXIMUM_PAGE_SIZE = 50;

    private static final ZoneId BUSINESS_TIME_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final AppointmentRepository appointmentRepository;

    private final AvailabilityOccurrenceRepository
            availabilityOccurrenceRepository;

    private final DoctorAvailabilityRepository
            doctorAvailabilityRepository;

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final DoctorReviewRepository
            doctorReviewRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailabilityOccurrenceRepository
                    availabilityOccurrenceRepository,
            DoctorAvailabilityRepository
                    doctorAvailabilityRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            DoctorReviewRepository doctorReviewRepository
    ) {
        this.appointmentRepository =
                appointmentRepository;

        this.availabilityOccurrenceRepository =
                availabilityOccurrenceRepository;

        this.doctorAvailabilityRepository =
                doctorAvailabilityRepository;

        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;

        this.doctorReviewRepository =
                doctorReviewRepository;
    }

    @Transactional
    public AppointmentResponseDto bookAppointment(
            AppointmentRequestDto request,
            String patientEmail
    ) {
        Patient patient =
                findPatientByEmail(patientEmail);

        DoctorAvailability doctorAvailability =
                doctorAvailabilityRepository
                        .findById(
                                request.getDoctorAvailabilityId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor availability slot not found."
                                )
                        );

        if (!doctorAvailability.isActive()) {
            throw new IllegalStateException(
                    "This doctor availability is no longer active."
            );
        }

        if (doctorAvailability.getDoctor().getVerificationStatus()
                != VerificationStatus.APPROVED) {
            throw new SecurityException(
                    "Appointments can only be booked with an approved doctor."
            );
        }

        LocalDate appointmentDate =
                request.getAppointmentDate();

        validateAppointmentDate(
                appointmentDate,
                doctorAvailability
        );

        AvailabilityOccurrence occurrence =
                findOrCreateOccurrence(
                        doctorAvailability,
                        appointmentDate
                );

        boolean alreadyBooked =
                appointmentRepository
                        .existsByPatientAndAvailabilityOccurrenceAndStatusNot(
                                patient,
                                occurrence,
                                AppointmentStatus.CANCELLED
                        );

        if (alreadyBooked) {
            throw new IllegalStateException(
                    "You already have an active appointment for this slot."
            );
        }

        if (
                occurrence.getBookedPatients()
                        >= doctorAvailability
                        .getMaxPatientsAllowed()
        ) {
            throw new IllegalStateException(
                    "This appointment slot is fully booked."
            );
        }

        int nextQueueNumber =
                occurrence.getLastQueueNumber() + 1;

        occurrence.setLastQueueNumber(
                nextQueueNumber
        );

        occurrence.setBookedPatients(
                occurrence.getBookedPatients() + 1
        );

        Appointment appointment = new Appointment();

        appointment.setPatient(patient);

        appointment.setAvailabilityOccurrence(
                occurrence
        );

        appointment.setQueueNumber(nextQueueNumber);

        appointment.setStatus(
                AppointmentStatus.BOOKED
        );

        appointment.setReasonForVisit(
                normalizeOptionalText(
                        request.getReasonForVisit()
                )
        );

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return toResponse(savedAppointment);
    }

    @Transactional
    public Page<AppointmentResponseDto>
    getPatientAppointments(
            String patientEmail,
            AppointmentStatus status,
            int page,
            int size
    ) {
        validatePagination(page, size);

        Patient patient =
                findPatientByEmail(patientEmail);

        PageRequest pageRequest =
                PageRequest.of(page, size);

        if (status == AppointmentStatus.BOOKED) {
            LocalDateTime now = currentDateTime();

            return appointmentRepository
                    .findPatientUpcomingAppointments(
                            patient,
                            now.toLocalDate(),
                            now.toLocalTime(),
                            pageRequest
                    )
                    .map(this::toResponse);
        }

        return appointmentRepository
                .findPatientAppointments(
                        patient,
                        status,
                        pageRequest
                )
                .map(this::toResponse);
    }

    @Transactional
    public Page<AppointmentResponseDto>
    getDoctorAppointments(
            String doctorEmail,
            AppointmentStatus status,
            DoctorAppointmentScope scope,
            int page,
            int size
    ) {
        validatePagination(page, size);

        if (status != null && scope != null) {
            throw new IllegalArgumentException(
                    "Use either status or scope, not both."
            );
        }

        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        requireApprovedDoctor(doctor);

        PageRequest pageRequest =
                PageRequest.of(page, size);

        if (scope == null) {
            return appointmentRepository
                    .findDoctorAppointments(
                            doctor,
                            status,
                            pageRequest
                    )
                    .map(this::toResponse);
        }

        LocalDateTime currentDateTime =
                currentDateTime();

        return switch (scope) {
            case TODAY ->
                    appointmentRepository
                            .findDoctorTodayAppointments(
                                    doctor,
                                    currentDateTime
                                            .toLocalDate(),
                                    currentDateTime
                                            .toLocalTime(),
                                    pageRequest
                            )
                            .map(this::toResponse);

            case UPCOMING ->
                    appointmentRepository
                            .findDoctorUpcomingAppointments(
                                    doctor,
                                    currentDateTime
                                            .toLocalDate(),
                                    pageRequest
                            )
                            .map(this::toResponse);

            case NEEDS_ACTION ->
                    appointmentRepository
                            .findDoctorAppointmentsNeedingAction(
                                    doctor,
                                    currentDateTime
                                            .toLocalDate(),
                                    currentDateTime
                                            .toLocalTime(),
                                    pageRequest
                            )
                            .map(this::toResponse);

            case COMPLETED ->
                    appointmentRepository
                            .findDoctorAppointments(
                                    doctor,
                                    AppointmentStatus.COMPLETED,
                                    pageRequest
                            )
                            .map(this::toResponse);

            case CANCELLED ->
                    appointmentRepository
                            .findDoctorAppointments(
                                    doctor,
                                    AppointmentStatus.CANCELLED,
                                    pageRequest
                            )
                            .map(this::toResponse);

            case NO_SHOW ->
                    appointmentRepository
                            .findDoctorAppointments(
                                    doctor,
                                    AppointmentStatus.NO_SHOW,
                                    pageRequest
                            )
                            .map(this::toResponse);
        };
    }

    @Transactional
    public DoctorAppointmentSummaryDto
    getDoctorAppointmentSummary(
            String doctorEmail
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        requireApprovedDoctor(doctor);

        LocalDateTime currentDateTime =
                currentDateTime();

        LocalDate currentDate =
                currentDateTime.toLocalDate();

        return DoctorAppointmentSummaryDto
                .builder()
                .today(
                        appointmentRepository
                                .countDoctorTodayAppointments(
                                        doctor,
                                        currentDate,
                                        currentDateTime
                                                .toLocalTime()
                                )
                )
                .upcoming(
                        appointmentRepository
                                .countDoctorUpcomingAppointments(
                                        doctor,
                                        currentDate
                                )
                )
                .needsAction(
                        appointmentRepository
                                .countDoctorAppointmentsNeedingAction(
                                        doctor,
                                        currentDate,
                                        currentDateTime
                                                .toLocalTime()
                                )
                )
                .completed(
                        appointmentRepository
                                .countDoctorAppointmentsByStatus(
                                        doctor,
                                        AppointmentStatus.COMPLETED
                                )
                )
                .cancelled(
                        appointmentRepository
                                .countDoctorAppointmentsByStatus(
                                        doctor,
                                        AppointmentStatus.CANCELLED
                                )
                )
                .noShow(
                        appointmentRepository
                                .countDoctorAppointmentsByStatus(
                                        doctor,
                                        AppointmentStatus.NO_SHOW
                                )
                )
                .build();
    }

    @Transactional
    public AppointmentResponseDto cancelAppointment(
            Long appointmentId,
            String patientEmail,
            AppointmentCancellationRequest request
    ) {
        Patient patient =
                findPatientByEmail(patientEmail);

        Appointment appointment =
                appointmentRepository
                        .findByIdForUpdate(appointmentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Appointment not found."
                                )
                        );

        if (
                !appointment.getPatient()
                        .getPatientId()
                        .equals(patient.getPatientId())
        ) {
            throw new SecurityException(
                    "You are not allowed to cancel this appointment."
            );
        }

        if (
                appointment.getStatus()
                        != AppointmentStatus.BOOKED
        ) {
            throw new IllegalStateException(
                    "Only a booked appointment can be cancelled."
            );
        }

        AvailabilityOccurrence occurrence =
                appointment
                        .getAvailabilityOccurrence();

        DoctorAvailability availability =
                occurrence.getDoctorAvailability();

        LocalDateTime appointmentStart =
                LocalDateTime.of(
                        occurrence.getAppointmentDate(),
                        availability.getStartTime()
                );

        LocalDateTime now = currentDateTime();

        if (!now.isBefore(appointmentStart)) {
            throw new IllegalStateException(
                    "This appointment can no longer be cancelled "
                            + "because its session has already started."
            );
        }

        AvailabilityOccurrence lockedOccurrence =
                availabilityOccurrenceRepository
                        .findByIdForUpdate(
                                occurrence.getOccurrenceId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Appointment occurrence not found."
                                )
                        );

        lockedOccurrence.setBookedPatients(
                Math.max(
                        lockedOccurrence
                                .getBookedPatients() - 1,
                        0
                )
        );

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointment.setCancellationReason(
                normalizeOptionalText(
                        request.getReason()
                )
        );

        appointment.setCancelledAt(now);

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDto completeAppointment(
            Long appointmentId,
            String doctorEmail
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        requireApprovedDoctor(doctor);

        Appointment appointment =
                findDoctorOwnedAppointment(
                        appointmentId,
                        doctor
                );

        if (
                appointment.getStatus()
                        != AppointmentStatus.BOOKED
        ) {
            throw new IllegalStateException(
                    "Only a booked appointment can be completed."
            );
        }

        AvailabilityOccurrence occurrence =
                appointment
                        .getAvailabilityOccurrence();

        DoctorAvailability availability =
                occurrence.getDoctorAvailability();

        LocalDateTime appointmentStart =
                LocalDateTime.of(
                        occurrence.getAppointmentDate(),
                        availability.getStartTime()
                );

        LocalDateTime now = currentDateTime();

        if (now.isBefore(appointmentStart)) {
            throw new IllegalStateException(
                    "A future appointment cannot be completed."
            );
        }

        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        appointment.setCompletedAt(now);

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDto
    markAppointmentAsNoShow(
            Long appointmentId,
            String doctorEmail
    ) {
        Doctor doctor =
                findDoctorByEmail(doctorEmail);

        requireApprovedDoctor(doctor);

        Appointment appointment =
                findDoctorOwnedAppointment(
                        appointmentId,
                        doctor
                );

        if (
                appointment.getStatus()
                        != AppointmentStatus.BOOKED
        ) {
            throw new IllegalStateException(
                    "Only a booked appointment can be marked as no-show."
            );
        }

        AvailabilityOccurrence occurrence =
                appointment
                        .getAvailabilityOccurrence();

        DoctorAvailability availability =
                occurrence.getDoctorAvailability();

        LocalDateTime appointmentEnd =
                LocalDateTime.of(
                        occurrence.getAppointmentDate(),
                        availability.getEndTime()
                );

        LocalDateTime now = currentDateTime();

        if (now.isBefore(appointmentEnd)) {
            throw new IllegalStateException(
                    "An appointment cannot be marked as no-show "
                            + "before its session ends."
            );
        }

        appointment.setStatus(
                AppointmentStatus.NO_SHOW
        );

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return toResponse(savedAppointment);
    }

    private Appointment findDoctorOwnedAppointment(
            Long appointmentId,
            Doctor doctor
    ) {
        Appointment appointment =
                appointmentRepository
                        .findByIdForUpdate(appointmentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Appointment not found."
                                )
                        );

        Long appointmentDoctorId =
                appointment
                        .getAvailabilityOccurrence()
                        .getDoctorAvailability()
                        .getDoctor()
                        .getDoctorId();

        if (
                !appointmentDoctorId.equals(
                        doctor.getDoctorId()
                )
        ) {
            throw new SecurityException(
                    "You are not allowed to manage this appointment."
            );
        }

        return appointment;
    }

    private Patient findPatientByEmail(String email) {
        return patientRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Patient profile not found."
                        )
                );
    }

    private Doctor findDoctorByEmail(String email) {
        return doctorRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Doctor profile not found."
                        )
                );
    }

    private void validateAppointmentDate(
            LocalDate appointmentDate,
            DoctorAvailability doctorAvailability
    ) {
        LocalDate currentDate =
                currentDateTime().toLocalDate();

        if (appointmentDate.isBefore(currentDate)) {
            throw new IllegalArgumentException(
                    "Appointment date cannot be in the past."
            );
        }

        if (
                appointmentDate.getDayOfWeek()
                        != doctorAvailability.getDayOfWeek()
        ) {
            throw new IllegalArgumentException(
                    "Doctor is not available on the selected date."
            );
        }

        if (appointmentDate.equals(currentDate)
                && !currentDateTime().toLocalTime()
                    .isBefore(doctorAvailability.getStartTime())) {
            throw new IllegalArgumentException(
                    "This appointment session has already started."
            );
        }

        LocalDate maximumAllowedDate =
                currentDate.plusWeeks(
                        MAXIMUM_ADVANCE_BOOKING_WEEKS
                );

        if (
                appointmentDate.isAfter(
                        maximumAllowedDate
                )
        ) {
            throw new IllegalArgumentException(
                    "You can only book appointments up to "
                            + MAXIMUM_ADVANCE_BOOKING_WEEKS
                            + " weeks in advance."
            );
        }
    }

    private AvailabilityOccurrence
    findOrCreateOccurrence(
            DoctorAvailability doctorAvailability,
            LocalDate appointmentDate
    ) {
        Optional<AvailabilityOccurrence>
                existingOccurrence =
                availabilityOccurrenceRepository
                        .findByDoctorAvailabilityAndAppointmentDate(
                                doctorAvailability,
                                appointmentDate
                        );

        if (existingOccurrence.isPresent()) {
            return existingOccurrence.get();
        }

        try {
            AvailabilityOccurrence newOccurrence =
                    new AvailabilityOccurrence();

            newOccurrence.setDoctorAvailability(
                    doctorAvailability
            );

            newOccurrence.setAppointmentDate(
                    appointmentDate
            );

            newOccurrence.setBookedPatients(0);
            newOccurrence.setLastQueueNumber(0);

            return availabilityOccurrenceRepository
                    .saveAndFlush(newOccurrence);

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new IllegalStateException(
                    "This appointment slot is currently being booked. "
                            + "Please try again."
            );
        }
    }

    private AppointmentResponseDto toResponse(
            Appointment appointment
    ) {
        AvailabilityOccurrence occurrence =
                appointment
                        .getAvailabilityOccurrence();

        DoctorAvailability availability =
                occurrence.getDoctorAvailability();

        Doctor doctor = availability.getDoctor();
        Patient patient = appointment.getPatient();

        String doctorName =
                doctor.getUser().getFirstName()
                        + " "
                        + doctor.getUser().getLastName();

        String patientName =
                patient.getUser().getFirstName()
                        + " "
                        + patient.getUser().getLastName();

        boolean reviewSubmitted =
                doctorReviewRepository
                        .existsByAppointment(appointment);

        return AppointmentResponseDto
                .builder()
                .appointmentId(
                        appointment.getAppointmentId()
                )
                .doctorId(
                        doctor.getDoctorId()
                )
                .doctorName(doctorName)
                .doctorProfileImageUrl(
                        doctor.getProfileImageUrl()
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .patientId(
                        patient.getPatientId()
                )
                .patientName(patientName)
                .appointmentDate(
                        occurrence.getAppointmentDate()
                )
                .startTime(
                        availability.getStartTime()
                )
                .endTime(
                        availability.getEndTime()
                )
                .queueNumber(
                        appointment.getQueueNumber()
                )
                .status(
                        appointment.getStatus()
                )
                .reasonForVisit(
                        appointment.getReasonForVisit()
                )
                .cancellationReason(
                        appointment.getCancellationReason()
                )
                .reviewSubmitted(reviewSubmitted)
                .build();
    }

    private void validatePagination(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50."
            );
        }
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private LocalDateTime currentDateTime() {
        return LocalDateTime.now(
                BUSINESS_TIME_ZONE
        );
    }

    private void requireApprovedDoctor(Doctor doctor) {
        if (doctor.getVerificationStatus()
                != VerificationStatus.APPROVED) {
            throw new SecurityException(
                    "Your doctor profile must be approved before managing appointments."
            );
        }
    }
}
