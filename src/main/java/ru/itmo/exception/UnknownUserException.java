package ru.itmo.exception;

public class UnknownUserException extends Exception {
    public UnknownUserException(String message) {
        super(message);
    }

    public UnknownUserException(String message, Throwable cause) {
        super(message, cause);
    }
}