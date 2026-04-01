package ru.itmo.domain.model;

import org.junit.jupiter.api.Test;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentTest {
    private static final Timestamp timestamp1 = Timestamp.from(Instant.now());
    private static final Timestamp timestamp2 = Timestamp.from(Instant.now().minusSeconds(10));

    @Test
    void testValidUpdate() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.NOT_SPECIFIED,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null
                , null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_MARKINA,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.isEmpty());
        assertEquals(12345, student.getIsu());
        assertEquals("A1", student.getStGroup());
        assertEquals("Иванов Иван", student.getFullName());
    }

    @Test
    void testInvalidPhoneNumber() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "123456789L0", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("номер телефона должен начинаться с +7 или 8."));
    }

    @Test
    void testInvalidPracticeFormat() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.OFFLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("для компаний не из Питера формат практики только \"Удаленный\""));
    }

    @Test
    void testInvalidStatusTransition() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.COMPANY_INFO_WAITING_APPROVAL,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("переход из статуса \"Зарегистрирован\" в статус \"Данные о компании на проверке\" невозможен"));
    }

    @Test
    void testMissingRequiredFields() {
        Student student = new Student(
                null, null, 0, null, null, StudentStatus.REGISTERED,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                0, null, null, StudentStatus.REGISTERED,
                null, null, null, null, null, null, null, null, null, null, null, null, "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("не все поля для статуса \"Зарегистрирован\" заполнены"));
    }

    @Test
    void testInvalidCompanyINNForNonPetersburgCompany() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.OFFLINE, 100000000L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("для компаний не из Питера формат практики только \"Удаленный\""));
    }

    @Test
    void testValidPracticeFormatForPetersburgCompany() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_MARKINA,
                PracticeFormat.ONLINE, 781234567L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testMissingStudentGroup() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, null, "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("не все поля для статуса \"Зарегистрирован\" заполнены"));
    }

    @Test
    void testInvalidStatusChange() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.COMPANY_INFO_WAITING_APPROVAL,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertEquals("переход из статуса \"Практика в ИТМО у Маркиной Т. А.\" в статус \"Данные о компании на проверке\" невозможен", errors.get(0));
    }

    @Test
    void testValidStatusChange() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.COMPANY_INFO_WAITING_APPROVAL,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.COMPANY_INFO_RETURNED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.isEmpty());
        assertEquals(StudentStatus.COMPANY_INFO_RETURNED, student.getStatus());
    }

    @Test
    void testPracticePlace() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_MARKINA,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testMissingCompanyLeadFullName() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_UNIVERSITY,
                null, null, null, null, null, null, null, null, null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.contains("не все поля для статуса \"Практика в ИТМО у Маркиной Т. А.\" заполнены"));
    }

    @Test
    void testValidPhoneNumber() {
        Student student = new Student(
                null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.NOT_SPECIFIED,
                PracticeFormat.ONLINE, null, null, null, 123456789L, "Компания", "Руководитель",
                "+7 123 456 7890", "lead@company.com", "Руководитель", "#FFFFFF", false, timestamp1, timestamp2, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "applications", "notifications", "Комментарий", "Комментарий звонка", PracticePlace.ITMO_MARKINA,
                PracticeFormat.ONLINE, 123456789L, "Компания", "Руководитель",
                "+7 987 654 3210", "lead@company.com", "Руководитель", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);
        assertTrue(errors.isEmpty());
    }
}
