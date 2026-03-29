package se.ifmo.ru.exception;

public class CsvReadException extends CompanyServiceException {
    public CsvReadException(String message) {
        super(message);
    }

    public CsvReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
