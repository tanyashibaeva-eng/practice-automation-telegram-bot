package ru.itmo.domain.type;

import lombok.AllArgsConstructor;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.Arrays;
import java.util.List;
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
    /* 10 */ APPLICATION_SIGNED("APPLICATION_SIGNED");

    private final String name;

    public static StudentStatus valueOfIgnoreCase(String name) {
        return valueOf(name.trim().toUpperCase());
    }

    public static StudentStatus getByUserName(String text) {
        for (StudentStatus status : StudentStatus.values()) {
            if (status.getUserName().equals(text)) {
                return status;
            }
        }
        return null;
    }

    public String getUserName() {
        return switch (this) {
            case NOT_REGISTERED -> "Не зарегистрирован";
            case REGISTERED -> "Зарегистрирован";
            case PRACTICE_IN_ITMO_MARKINA -> "Практика в ИТМО у Маркиной Т. А.";
            case COMPANY_INFO_WAITING_APPROVAL -> "Данные о компании на проверке";
            case COMPANY_INFO_RETURNED -> "Данные о компании возвращены на доработку";
            case APPLICATION_WAITING_SUBMISSION -> "Данные о компании утверждены и ожидается заполнения заявки";
            case PRACTICE_APPROVED -> "Практика согласована";
            case APPLICATION_WAITING_APPROVAL -> "Заявка на проверке";
            case APPLICATION_RETURNED -> "Заявка возвращена на доработку";
            case APPLICATION_WAITING_SIGNING -> "Заявка согласована и ожидает подписания";
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
            case APPLICATION_SIGNED -> 41;
        };
    }
}
