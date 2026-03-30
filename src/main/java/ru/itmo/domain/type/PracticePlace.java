package ru.itmo.domain.type;

import lombok.AllArgsConstructor;
import ru.itmo.exception.BadRequestException;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum PracticePlace {
    NOT_SPECIFIED("NOT_SPECIFIED"),
    ITMO_MARKINA("ITMO_MARKINA"),
    ITMO_UNIVERSITY("ITMO_UNIVERSITY"),
    OTHER_COMPANY("OTHER_COMPANY");

    private final String name;

    public static PracticePlace valueOfIgnoreCaseChecked(String name) throws BadRequestException {
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("неизвестное место практики: %s, ".formatted(name) + getAvailableValues());
        }
    }

    public static PracticePlace valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }

    public static PracticePlace getByDisplayName(String text) {
        for (PracticePlace place : PracticePlace.values()) {
            if (place.getDisplayName().equals(text)) {
                return place;
            }
        }
        return NOT_SPECIFIED;
    }

    public String getDisplayName() {
        return switch (this) {
            case NOT_SPECIFIED -> "";
            case ITMO_MARKINA -> "Практика в ИТМО";
            case ITMO_UNIVERSITY -> "Практика в лаборатории ИТМО";
            case OTHER_COMPANY -> "Практика в сторонней компании";
        };
    }

    public static String getAvailableValues() {
        return "доступные значения:\n"
                + Arrays.stream(PracticePlace.values())
                .map(value -> "%s : %s".formatted(value, value.getDisplayName().isBlank() ? "Не указано" : value.getDisplayName()))
                .collect(Collectors.joining("\n"));
    }
}
