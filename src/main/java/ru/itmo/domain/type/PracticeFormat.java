package ru.itmo.domain.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PracticeFormat {
    NOT_SPECIFIED("NOT_SPECIFIED"),
    OFFLINE("OFFLINE"),
    HYBRID("HYBRID"),
    ONLINE("ONLINE");

    private final String name;

    public static PracticeFormat valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }

    public String getUserName() {
        return switch (this) {
            case NOT_SPECIFIED -> "";
            case OFFLINE -> "Очный";
            case HYBRID -> "Гибридный";
            case ONLINE -> "Удаленный";
        };
    }
}
