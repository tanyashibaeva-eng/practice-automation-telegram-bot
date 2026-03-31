package se.ifmo.ru.exception;

public class CompanyNotFoundException extends CompanyServiceException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
