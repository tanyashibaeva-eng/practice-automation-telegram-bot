package se.ifmo.ru.exception;

public class CompanyServiceException extends Exception {
    public CompanyServiceException(String message) {
        super(message);
    }

    public CompanyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
