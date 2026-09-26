package com.nexcare.backend.service;

import com.nexcare.backend.dto.AppointmentRequestDto;
import com.nexcare.backend.dto.AppointmentResponseDto;
import com.nexcare.backend.entity.Appointment;
import com.nexcare.backend.entity.AppointmentStatus;
import com.nexcare.backend.entity.AvailabilityOccurrence;
import com.nexcare.backend.entity.DoctorAvailability;
import com.nexcare.backend.entity.Patient;
import com.nexcare.backend.repository.AppointmentRepository;
import com.nexcare.backend.repository.AvailabilityOccurrenceRepository;
import com.nexcare.backend.repository.DoctorAvailabilityRepository;
import com.nexcare.backend.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityOccurrenceRepository availabilityOccurrenceRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailabilityOccurrenceRepository availabilityOccurrenceRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            PatientRepository patientRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityOccurrenceRepository = availabilityOccurrenceRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public AppointmentResponseDto bookAppointment(
            AppointmentRequestDto request,
            String patientEmail
    ) {

        /*
         * STEP 1
         * Find the patient associated with the authenticated user.
         *
         * patientEmail comes from JWT/Spring Security.
         * The client does NOT provide the patient identity.
         */
        Patient patient = patientRepository.findByUserEmail(patientEmail)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Patient profile not found for this user."
                        )
                );


        /*
         * STEP 2
         * Find the doctor's recurring availability rule.
         *
         * Example:
         * Availability ID = 17
         * Monday
         * 09:00 - 13:00
         * maxPatientsAllowed = 5
         */
        DoctorAvailability doctorAvailability =
                doctorAvailabilityRepository
                        .findById(request.getDoctorAvailabilityId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Doctor availability slot not found."
                                )
                        );


        /*
         * STEP 3
         * Validate the requested date.
         *
         * 3.1 The selected date must fall on the same
         *     day of the week configured in DoctorAvailability.
         */
        LocalDate appointmentDate = request.getAppointmentDate();

        if (appointmentDate.getDayOfWeek()
                != doctorAvailability.getDayOfWeek()) {

            throw new IllegalArgumentException(
                    "Doctor is not available on the selected date."
            );
        }


        /*
         * 3.2 V1 booking window:
         * Patients can book only up to 3 weeks in advance.
         *
         * Frontend may disable dates beyond this range,
         * but backend validation is mandatory because
         * clients such as Postman can bypass frontend validation.
         */
        LocalDate maxAllowedDate =
                LocalDate.now().plusWeeks(3);

        if (appointmentDate.isAfter(maxAllowedDate)) {

            throw new IllegalArgumentException(
                    "You can only book appointments up to 3 weeks in advance."
            );
        }


        /*
         * STEP 4
         * Find the AvailabilityOccurrence for:
         *
         *     DoctorAvailability + AppointmentDate
         *
         * If the occurrence already exists:
         *     PESSIMISTIC_WRITE locks that database row.
         *
         * If it does not exist:
         *     Create it lazily.
         *
         * The database UNIQUE constraint on
         * (doctor_availability_id, appointment_date)
         * protects against duplicate occurrence creation.
         */
        AvailabilityOccurrence occurrence;

        Optional<AvailabilityOccurrence> existingOccurrence =
                availabilityOccurrenceRepository
                        .findByDoctorAvailabilityAndAppointmentDate(
                                doctorAvailability,
                                appointmentDate
                        );

        if (existingOccurrence.isPresent()) {

            /*
             * Existing row is already locked because the repository
             * method uses:
             *
             * @Lock(LockModeType.PESSIMISTIC_WRITE)
             */
            occurrence = existingOccurrence.get();

        } else {

            try {

                AvailabilityOccurrence newOccurrence =
                        new AvailabilityOccurrence();

                newOccurrence.setDoctorAvailability(
                        doctorAvailability
                );

                newOccurrence.setAppointmentDate(
                        appointmentDate
                );

                /*
                 * saveAndFlush() forces the INSERT to reach
                 * PostgreSQL immediately.
                 *
                 * This allows the UNIQUE constraint to detect
                 * a concurrent occurrence creation attempt.
                 */
                occurrence =
                        availabilityOccurrenceRepository
                                .saveAndFlush(newOccurrence);

            } catch (DataIntegrityViolationException e) {

                /*
                 * Another transaction may have created the same
                 * occurrence concurrently.
                 *
                 * For V1 we fail safely and ask the patient
                 * to retry rather than attempting to continue
                 * inside the failed transaction.
                 */
                throw new IllegalStateException(
                        "This appointment slot is currently being booked. Please try again."
                );
            }
        }


        /*
         * STEP 5
         * Prevent the same patient from booking the same
         * occurrence more than once.
         *
         * CANCELLED appointments are ignored, so a patient
         * can book again after cancelling.
         */
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


        /*
         * STEP 6
         * Check whether the occurrence has reached capacity.
         *
         * bookedPatients = currently active patients.
         *
         * lastQueueNumber is intentionally separate because
         * queue numbers are never reused after cancellation.
         */
        if (occurrence.getBookedPatients()
                >= doctorAvailability.getMaxPatientsAllowed()) {

            throw new IllegalStateException(
                    "This appointment slot is fully booked."
            );
        }


        /*
         * STEP 7
         * Generate the next queue number.
         *
         * Example:
         *
         * lastQueueNumber = 4
         * nextQueueNumber = 5
         *
         * Because the occurrence is pessimistically locked,
         * concurrent transactions cannot safely modify the
         * same counters at the same time.
         */
        int nextQueueNumber =
                occurrence.getLastQueueNumber() + 1;

        occurrence.setLastQueueNumber(
                nextQueueNumber
        );

        /*
         * Increase currently active patient count.
         */
        occurrence.setBookedPatients(
                occurrence.getBookedPatients() + 1
        );


        /*
         * STEP 8
         * Create the actual appointment.
         *
         * Notice:
         *
         * Appointment does NOT store:
         * - doctor
         * - appointmentDate
         * - startTime
         * - endTime
         *
         * It points to AvailabilityOccurrence.
         *
         * Appointment
         *      ↓
         * AvailabilityOccurrence
         *      ↓
         * DoctorAvailability
         *      ↓
         * Doctor
         */
        Appointment appointment =
                new Appointment();

        appointment.setPatient(patient);

        appointment.setAvailabilityOccurrence(
                occurrence
        );

        appointment.setQueueNumber(
                nextQueueNumber
        );

        appointment.setStatus(
                AppointmentStatus.BOOKED
        );


        /*
         * STEP 9
         * Persist the appointment.
         *
         * The occurrence is already a managed JPA entity,
         * so its changed counters will be detected by
         * Hibernate's dirty checking.
         */
        Appointment savedAppointment =
                appointmentRepository.save(appointment);


        /*
         * STEP 10
         * Build the response DTO.
         *
         * We return useful information to the frontend
         * without exposing JPA entities.
         */
        String doctorName =
                doctorAvailability.getDoctor()
                        .getUser()
                        .getFirstName()
                        + " "
                        + doctorAvailability.getDoctor()
                        .getUser()
                        .getLastName();

        String timeWindow =
                doctorAvailability.getStartTime()
                        + " - "
                        + doctorAvailability.getEndTime();


        /*
         * STEP 11
         * Transaction commits after the method successfully
         * completes.
         *
         * At commit:
         *
         * 1. Appointment INSERT
         * 2. AvailabilityOccurrence UPDATE
         *
         * Both succeed together.
         *
         * If an exception occurs before commit,
         * the transaction rolls back.
         */
        return AppointmentResponseDto.builder()
                .appointmentId(
                        savedAppointment.getAppointmentId()
                )
                .doctorName(doctorName)
                .appointmentDate(
                        occurrence.getAppointmentDate()
                )
                .timeWindow(timeWindow)
                .queueNumber(
                        savedAppointment.getQueueNumber()
                )
                .status(
                        savedAppointment.getStatus().name()
                )
                .build();
    }
}