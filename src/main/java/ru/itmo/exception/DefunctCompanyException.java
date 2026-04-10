package ru.itmo.exception;

public class DefunctCompanyException extends RuntimeException {
    public DefunctCompanyException(String message) {
        super(message);
    }
}
