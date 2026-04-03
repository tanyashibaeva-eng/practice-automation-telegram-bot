package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeadInfoField {
    FULLNAME("ФИО руководителя", "company_lead_fullname"),
    LASTNAME("Фамилию руководителя", "company_lead_fullname"),
    FIRSTNAME("Имя руководителя", "company_lead_fullname"),
    PATRONYMIC("Отчество руководителя", "company_lead_fullname"),
    PHONE("Телефон руководителя", "company_lead_phone"),
    EMAIL("Email руководителя", "company_lead_email"),
    JOB_TITLE("Должность руководителя", "company_lead_job_title");

    private final String displayName;
    private final String column;

    public static LeadInfoField fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isNamePart() {
        return this == LASTNAME || this == FIRSTNAME || this == PATRONYMIC;
    }
}
