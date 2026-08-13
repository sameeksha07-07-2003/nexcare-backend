package com.nexcare.backend.exception;

public class InvalidSignupRequestException extends RuntimeException {

    public InvalidSignupRequestException(String message) {
        super(message);
    }
}