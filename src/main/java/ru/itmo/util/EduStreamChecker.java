package ru.itmo.util;

import ru.itmo.domain.model.EduStream;

import java.time.LocalDate;

public class EduStreamChecker {
    public static boolean isActiveStream(EduStream stream) {
        return stream.getDateTo().isAfter(LocalDate.now());
    }
}
