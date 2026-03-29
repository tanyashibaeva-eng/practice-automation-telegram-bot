package ru.itmo.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.itmo.domain.model.CompanyApprovalRequest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovedCompanyRegistryServiceTest {

    @AfterEach
    void tearDown() {
        ApprovedCompanyRegistryService.resetForTests();
    }

    @Test
    void hasOfficeInSaintPetersburg_returnsTrueForExistingInn(@TempDir Path tempDir) throws Exception {
        Path csv = createCsv(tempDir);
        ApprovedCompanyRegistryService.overrideCsvPathForTests(csv);

        boolean result = ApprovedCompanyRegistryService.hasOfficeInSaintPetersburg(7800000000L);

        assertTrue(result);
    }

    @Test
    void hasOfficeInSaintPetersburg_returnsFalseForMissingInn(@TempDir Path tempDir) throws Exception {
        Path csv = createCsv(tempDir);
        ApprovedCompanyRegistryService.overrideCsvPathForTests(csv);

        boolean result = ApprovedCompanyRegistryService.hasOfficeInSaintPetersburg(7811111111L);

        assertFalse(result);
    }

    @Test
    void saveApprovedCompany_persistsOnlySpbApprovalRequests(@TempDir Path tempDir) throws Exception {
        Path csv = createCsv(tempDir);
        ApprovedCompanyRegistryService.overrideCsvPathForTests(csv);

        ApprovedCompanyRegistryService.saveApprovedCompany(CompanyApprovalRequest.builder()
                .inn(7811111111L)
                .companyName("Новая Компания")
                .companyAddress("Санкт-Петербург, Невский пр., д. 1")
                .requiresSpbOfficeApproval(true)
                .build());

        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        assertTrue(lines.stream().anyMatch(line -> line.contains("7811111111")));
    }

    @Test
    void saveApprovedCompany_skipsRegularApprovalRequests(@TempDir Path tempDir) throws Exception {
        Path csv = createCsv(tempDir);
        ApprovedCompanyRegistryService.overrideCsvPathForTests(csv);

        ApprovedCompanyRegistryService.saveApprovedCompany(CompanyApprovalRequest.builder()
                .inn(7811111111L)
                .companyName("Новая Компания")
                .companyAddress("Санкт-Петербург, Невский пр., д. 1")
                .requiresSpbOfficeApproval(false)
                .build());

        assertFalse(Files.readString(csv, StandardCharsets.UTF_8).contains("7811111111"));
    }

    private static Path createCsv(Path tempDir) throws Exception {
        Path csv = tempDir.resolve("SPARK_IT.csv");
        Files.write(csv, List.of(
                "Выборка компаний;;;;;;;",
                "№;Наименование;Регистрационный номер;Адрес (место нахождения);Код налогоплательщика;Код статистики;Регион регистрации;Вид деятельности/отрасль;2024, Среднесписочная численность работников",
                "1;Компания А;123;Санкт-Петербург, ул. Тестовая, д. 1;7800000000;111;Санкт-Петербург;ИТ;10"
        ), StandardCharsets.UTF_8);
        return csv;
    }
}
