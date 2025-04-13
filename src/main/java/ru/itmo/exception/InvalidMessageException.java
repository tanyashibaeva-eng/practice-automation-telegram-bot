package ru.itmo.exception;

public class InvalidMessageException extends Exception {

    public InvalidMessageException(String message) {
        super(message + "\nПопробуйте еще раз или вернитесь назад");
    }

    public InvalidMessageException() {
        super("Я не понимаю вас. Попробуйте еще раз или вернитесь назад");
    }

    public static void ThrowMessageException() throws InvalidMessageException {
        throw new InvalidMessageException("Я не понимаю вас.");
    }

    public static void ThrowDocumentException() throws InvalidMessageException {
        throw new InvalidMessageException("Я не понимаю вас, пожалуйста, загрузите файл.");
    }
}