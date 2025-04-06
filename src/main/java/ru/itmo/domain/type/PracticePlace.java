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

    public static PracticePlace getByUserName(String text) {
        for (PracticePlace place : PracticePlace.values()) {
            if (place.getUserName().equals(text)) {
                return place;
            }
        }
        return NOT_SPECIFIED;
    }

    public String getUserName() {
        return switch (this) {
            case NOT_SPECIFIED -> "";
            case ITMO_MARKINA -> "Практика в ИТМО";
            case ITMO_UNIVERSITY -> "Практика в лаборатории ИТМО";
            case OTHER_COMPANY -> "Практика в сторонней компании";
        };
    }
}
