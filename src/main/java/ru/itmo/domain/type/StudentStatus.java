package ru.itmo.domain.type;

import lombok.AllArgsConstructor;
import ru.itmo.exception.BadRequestException;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum StudentStatus {
    /* 0  */ NOT_REGISTERED("NOT_REGISTERED"),
    /* 1  */ REGISTERED("REGISTERED"),
    /* 2  */ PRACTICE_IN_ITMO_MARKINA("PRACTICE_IN_ITMO_MARKINA"),
    /* 3  */ COMPANY_INFO_WAITING_APPROVAL("COMPANY_INFO_WAITING_APPROVAL"),
    /* 4  */ COMPANY_INFO_RETURNED("COMPANY_INFO_RETURNED"),
    /* 5  */ PRACTICE_APPROVED("PRACTICE_APPROVED"),
    /* 6  */ APPLICATION_WAITING_SUBMISSION("APPLICATION_WAITING_SUBMISSION"),
    /* 7  */ APPLICATION_WAITING_APPROVAL("APPLICATION_WAITING_APPROVAL"),
    /* 8  */ APPLICATION_RETURNED("APPLICATION_RETURNED"),
    /* 9  */ APPLICATION_WAITING_SIGNING("APPLICATION_WAITING_SIGNING"),
    /* 10 */ APPLICATION_PHOTO_UPLOADED("APPLICATION_PHOTO_UPLOADED"),
    /* 11 */ APPLICATION_SIGNED("APPLICATION_SIGNED");

    private final String name;

    public static StudentStatus valueOfIgnoreCaseChecked(String name) throws BadRequestException {
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("неизвестный статус студента: %s, ".formatted(name) + getAvailableValues());
        }
    }

    public static StudentStatus valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }

    public static StudentStatus getByDisplayName(String text) {
        for (StudentStatus status : StudentStatus.values()) {
            if (status.getDisplayName().equals(text)) {
                return status;
            }
        }
        return null;
    }

    public String getDisplayName() {
        return switch (this) {
            case NOT_REGISTERED -> "Не зарегистрирован";
            case REGISTERED -> "Зарегистрирован";
            case PRACTICE_IN_ITMO_MARKINA -> "Практика в ИТМО у Маркиной Т. А.";
            case COMPANY_INFO_WAITING_APPROVAL -> "Данные о компании на проверке";
            case COMPANY_INFO_RETURNED -> "Данные о компании возвращены на доработку";
            case APPLICATION_WAITING_SUBMISSION -> "Данные о компании утверждены и ожидается заполнение заявки";
            case PRACTICE_APPROVED -> "Практика в ИТМО";
            case APPLICATION_WAITING_APPROVAL -> "Заявка на проверке";
            case APPLICATION_RETURNED -> "Заявка возвращена на доработку";
            case APPLICATION_WAITING_SIGNING -> "Заявка согласована и ожидает подписания";
            case APPLICATION_PHOTO_UPLOADED -> "Фото подписанной заявки загружено";
            case APPLICATION_SIGNED -> "Заявка подписана";
        };
    }

    public short getColorForStatus() {
        return switch (this) {
            case NOT_REGISTERED -> 31;
            case REGISTERED -> 26;
            case PRACTICE_IN_ITMO_MARKINA -> 50;
            case COMPANY_INFO_WAITING_APPROVAL -> 45;
            case COMPANY_INFO_RETURNED -> 43;
            case PRACTICE_APPROVED -> 42;
            case APPLICATION_WAITING_SUBMISSION -> 47;
            case APPLICATION_WAITING_APPROVAL -> 46;
            case APPLICATION_RETURNED -> 51;
            case APPLICATION_WAITING_SIGNING -> 44;
            case APPLICATION_PHOTO_UPLOADED -> 49;
            case APPLICATION_SIGNED -> 41;
        };
    }

    public static String getAvailableValues() {
        return "доступные значения:\n"
                + Arrays.stream(StudentStatus.values())
                .map(value -> "%s : %s".formatted(value, value.getDisplayName()))
                .collect(Collectors.joining("\n"));
    }
}