package se.ifmo.ru.exception;

public class CompanyAlreadyExistsException extends CompanyServiceException {
    public CompanyAlreadyExistsException(String message) {
        super(message);
    }
}
