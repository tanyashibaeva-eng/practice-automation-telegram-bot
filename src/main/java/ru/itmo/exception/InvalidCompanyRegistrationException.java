package ru.itmo.exception;

public class InvalidCompanyRegistrationException extends RuntimeException {
    public InvalidCompanyRegistrationException(String message) {
        super(message);
    }
}
