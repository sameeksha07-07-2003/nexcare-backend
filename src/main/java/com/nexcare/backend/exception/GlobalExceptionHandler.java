package com.nexcare.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            EmailAlreadyExistsException.class
    )
    public ResponseEntity<Object>
    handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            InvalidCredentialsException.class
    )
    public ResponseEntity<Object>
    handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            InvalidSignupRequestException.class
    )
    public ResponseEntity<Object>
    handleInvalidSignupRequest(
            InvalidSignupRequestException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<Object>
    handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult()
                        .getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed.",
                fieldErrors
        );
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<Object>
    handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Object>
    handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    /*
     * IllegalStateException currently represents business conflicts,
     * such as duplicate bookings, full capacity, or invalid appointment
     * state transitions.
     */
    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<Object>
    handleIllegalState(
            IllegalStateException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler({
            SecurityException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<Object>
    handleForbiddenOperation(
            Exception exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage() == null
                        ? "You are not allowed to perform this operation."
                        : exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<Object>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data.",
                null
        );
    }

    @ExceptionHandler(
            MaxUploadSizeExceededException.class
    )
    public ResponseEntity<Object>
    handleMaximumUploadSize(
            MaxUploadSizeExceededException exception
    ) {
        return buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File size must be less than 5 MB.",
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object>
    handleUnexpectedException(
            Exception exception
    ) {
        /*
         * Do not expose exception.getMessage() for unexpected server
         * failures. It may reveal internal implementation or database
         * information.
         */
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                null
        );
    }

    private ResponseEntity<Object> buildResponse(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                status.getReasonPhrase()
        );

        body.put(
                "message",
                message
        );

        if (validationErrors != null
                && !validationErrors.isEmpty()) {

            body.put(
                    "validationErrors",
                    validationErrors
            );
        }

        return ResponseEntity
                .status(status)
                .body(body);
    }
}