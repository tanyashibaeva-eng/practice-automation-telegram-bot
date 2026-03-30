package ru.itmo.domain.type;

import lombok.AllArgsConstructor;
import ru.itmo.exception.BadRequestException;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum PracticeFormat {
    NOT_SPECIFIED("NOT_SPECIFIED"),
    OFFLINE("OFFLINE"),
    HYBRID("HYBRID"),
    ONLINE("ONLINE");

    private final String name;

    public static PracticeFormat valueOfIgnoreCaseChecked(String name) throws BadRequestException {
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("неизвестный формат прохождения практики: %s, ".formatted(name) + getAvailableValues());
        }
    }

    public static PracticeFormat valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }

    public static PracticeFormat getByDisplayName(String text) {
        for (PracticeFormat format : PracticeFormat.values()) {
            if (format.getDisplayName().equals(text)) {
                return format;
            }
        }
        return NOT_SPECIFIED;
    }

    public String getDisplayName() {
        return switch (this) {
            case NOT_SPECIFIED -> "";
            case OFFLINE -> "Очный";
            case HYBRID -> "Гибридный";
            case ONLINE -> "Удаленный";
        };
    }

    public static String getAvailableValues() {
        return "доступные значения:\n"
                + Arrays.stream(PracticeFormat.values())
                .map(value -> "%s : %s".formatted(value, value.getDisplayName().isBlank() ? "Не указано" : value.getDisplayName()))
                .collect(Collectors.joining("\n"));
    }
}
