package se.ifmo.ru.exception;

public class InvalidCompanyDataException extends CompanyServiceException {
    public InvalidCompanyDataException(String message) {
        super(message);
    }
}
