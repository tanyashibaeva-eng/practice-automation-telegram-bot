package se.ifmo.ru.exception;

public class CsvWriteException extends CompanyServiceException {
    public CsvWriteException(String message) {
        super(message);
    }

    public CsvWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
