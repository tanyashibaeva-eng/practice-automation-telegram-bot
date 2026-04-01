package ru.itmo.infra.handler.usecase.admin.configureexport;

import ru.itmo.domain.model.Student;

public enum StudentColumn {

    CHAT_ID("chatID"),
    ISU("ИСУ"),
    GROUP("Группа"),
    FULLNAME("ФИО"),
    STATUS("Статус"),
    APPLICATION("Заявка"),
    NOTIFICATIONS("Уведомления"),
    COMMENT("Комментарий"),
    CALL_COMMENT("Комментарий по звонкам руководителю"),
    PRACTICE_PLACE("Место практики"),
    PRACTICE_FORMAT("Формат практики"),
    COMPANY_INN("ИНН Компании"),
    COMPANY_NAME("Компания"),
    COMPANY_LEAD("Руководитель"),
    COMPANY_PHONE("Телефон Руководителя"),
    COMPANY_EMAIL("Почта Руководителя"),
    COMPANY_POSITION("Должность Руководителя");

    private final String title;

    StudentColumn(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String extractValue(Student student, String groupName) {
        return switch (this) {
            case CHAT_ID -> student.getTelegramUser() != null ? student.getTelegramUser().getChatId() + "" : "";
            case ISU -> String.valueOf(student.getIsu());
            case GROUP -> groupName;
            case FULLNAME -> student.getFullName();
            case STATUS -> student.getStatus() != null ? student.getStatus().getDisplayName() : "";
            case APPLICATION -> student.getApplication() != null ? student.getApplication() : "";
            case NOTIFICATIONS -> student.getNotifications() != null ? student.getNotifications() : "";
            case COMMENT -> student.getComments() != null ? student.getComments() : "";
            case CALL_COMMENT -> student.getCallStatusComments() != null ? student.getCallStatusComments() : "";
            case PRACTICE_PLACE -> student.getPracticePlace() != null ? student.getPracticePlace().getDisplayName() : "";
            case PRACTICE_FORMAT -> student.getPracticeFormat() != null ? student.getPracticeFormat().getDisplayName() : "";
            case COMPANY_INN -> student.getCompanyINN() != null ? String.valueOf(student.getCompanyINN()) : "";
            case COMPANY_NAME -> student.getCompanyName() != null ? student.getCompanyName() : "";
            case COMPANY_LEAD -> student.getCompanyLeadFullName() != null ? student.getCompanyLeadFullName() : "";
            case COMPANY_PHONE -> student.getCompanyLeadPhone() != null ? student.getCompanyLeadPhone() : "";
            case COMPANY_EMAIL -> student.getCompanyLeadEmail() != null ? student.getCompanyLeadEmail() : "";
            case COMPANY_POSITION -> student.getCompanyLeadJobTitle() != null ? student.getCompanyLeadJobTitle() : "";
        };
    }
}

