package ru.itmo.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentPracticeFormatValidationTest {

    @AfterEach
    void tearDown() {
        ApprovedCompanyRegistryService.resetForTests();
    }

    @Test
    void updateOrGetErrors_allowsOfflineFormatForCompanyWithSpbOfficeInRegistry(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("SPARK_IT.csv");
        Files.write(csv, List.of(
                "header;;;;;;;",
                "No;Name;RegNumber;Address;TaxId;StatCode;Region;Activity;2024",
                "1;Company A;123;Saint Petersburg;1000000000;111;Saint Petersburg;IT;10"
        ), StandardCharsets.UTF_8);
        ApprovedCompanyRegistryService.overrideCsvPathForTests(csv);

        Timestamp exportedAt = Timestamp.from(Instant.now());
        Timestamp updatedAt = Timestamp.from(Instant.now().minusSeconds(10));

        Student student = new Student(
                null, null, 12345, "A1", "Ivan Ivanov", StudentStatus.REGISTERED,
                "comment", "call comment", PracticePlace.ITMO_MARKINA,
                PracticeFormat.ONLINE, 123456789L, "Company", "Lead",
                "+7 123 456 7890", "lead@company.com", "Lead", "#FFFFFF", false,
                exportedAt, updatedAt, null, null, false
        );

        ExcelStudentDTO dto = new ExcelStudentDTO(
                12345L,
                12345, "A1", "Ivan Ivanov", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                "", "", "comment", "call comment", PracticePlace.ITMO_MARKINA,
                PracticeFormat.OFFLINE, 1000000000L, "Company", "Lead",
                "+7 123 456 7890", "lead@company.com", "Lead", "#FFFFFF", null
        );

        List<String> errors = student.updateOrGetErrors(dto);

        assertTrue(errors.isEmpty());
    }
}
