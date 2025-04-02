package ru.itmo.domain.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StudentStatus {
    NOT_REGISTERED("NOT_REGISTERED"),
    REGISTERED("REGISTERED"),
    PRACTICE_IN_ITMO("PRACTICE_IN_ITMO"),
    COMPANY_INFO_WAITING_APPROVAL("COMPANY_INFO_WAITING_APPROVAL"),
    COMPANY_INFO_RETURNED("COMPANY_INFO_RETURNED"),
    APPLICATION_WAITING_SUBMISSION("APPLICATION_WAITING_SUBMISSION"),
    APPLICATION_RETURNED("APPLICATION_RETURNED"),
    APPLICATION_WAITING_SIGNING("APPLICATION_WAITING_SIGNING"),
    APPLICATION_SIGNED("APPLICATION_SIGNED");

    private final String name;

    public static StudentStatus valueOfIgnoreCase(String name) {
        return valueOf(name.toUpperCase());
    }

}
