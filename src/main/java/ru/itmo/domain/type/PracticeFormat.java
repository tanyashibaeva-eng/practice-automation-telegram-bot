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

    public static PracticeFormat getByUserName(String text) {
        for (PracticeFormat format : PracticeFormat.values()) {
            if (format.getUserName().equals(text)) {
                return format;
            }
        }
        return NOT_SPECIFIED;
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
