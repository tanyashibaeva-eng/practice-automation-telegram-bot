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
}
