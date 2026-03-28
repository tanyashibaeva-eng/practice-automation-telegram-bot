package se.ifmo.ru.exception;

public class InvalidInnException extends CompanyServiceException {
    public InvalidInnException(String message) {
        super(message);
    }
}
