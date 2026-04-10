package ru.itmo.exception;

public class InnNoLongerValidException extends RuntimeException {
    public InnNoLongerValidException(String message) {
        super(message);
    }
}
