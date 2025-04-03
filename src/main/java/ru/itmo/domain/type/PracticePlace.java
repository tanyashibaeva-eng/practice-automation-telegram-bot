package ru.itmo.domain.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PracticePlace {
    NOT_SPECIFIED("NOT_SPECIFIED"),
    ITMO_MARKINA("ITMO_MARKINA"),
    ITMO_UNIVERSITY("ITMO_UNIVERSITY"),
    OTHER_COMPANY("OTHER_COMPANY");

    private final String name;

    public static PracticePlace valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }
}
