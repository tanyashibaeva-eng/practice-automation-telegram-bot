package ru.itmo.domain.model;

import lombok.Getter;
import ru.itmo.exception.BadRequestException;

import java.util.UUID;

@Getter
public class AdminToken {

    private final UUID token;

    public AdminToken() {
        token = UUID.randomUUID();
    }

    public AdminToken(String uuidString) throws BadRequestException {
        try {
            token = UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Токен должен иметь формат UUID (стандарт RFC 4122), пример: '6948DF80-14BD-4E04-8842-7668D9C001F5'");
        }
    }

}
