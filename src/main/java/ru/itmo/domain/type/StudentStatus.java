package ru.itmo.domain.type;

import lombok.AllArgsConstructor;

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

}
