package ru.itmo.exception;

public class UnknownUserException extends Exception {
    public UnknownUserException(Long chatId) {
        super("user with chat id: " + chatId + " not found");
    }

    public UnknownUserException(String message, Throwable cause) {
        super(message, cause);
    }
}