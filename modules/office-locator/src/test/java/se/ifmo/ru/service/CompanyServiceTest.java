package se.ifmo.ru.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.ifmo.ru.parser.CompanyRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyServiceTest {
    private static Path createSampleCsv(Path dir) throws Exception {
        Path path = dir.resolve("companies.csv");
        List<String> lines = List.of(
                "№;Наименование;Регистрационный номер;Адрес (место нахождения);Код налогоплательщика;Код статистики;Регион регистрации;Вид деятельности/отрасль;2024, Среднесписочная численность работников",
                "1;Компания А;123;Адрес А;7700000000;111;Москва;ИТ;10",
                "2 000;Компания Б;456;Адрес Б;7800000000;222;СПб;ИТ;20"
        );
        Files.write(path, lines, StandardCharsets.UTF_8);
        return path;
    }

    @Test
    void findCompanyRecordByINN_returnsExisting(@TempDir Path tempDir) throws Exception {
        Path csv = createSampleCsv(tempDir);
        CompanyService service = new CompanyService(csv.toString());

        CompanyRecord record = service.findCompanyRecordByINN("7800000000");

        assertEquals("Компания Б", record.getName());
        assertEquals("Адрес Б", record.getAddress());
    }

    @Test
    void addCompanyRecord_persistsToCsv(@TempDir Path tempDir) throws Exception {
        Path csv = createSampleCsv(tempDir);
        CompanyService service = new CompanyService(csv.toString());

        service.addCompanyRecord("Яндекс", "7736207543", "Санкт-Петербург, пр. Пискаревский, д. 2");

        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        String lastLine = lines.get(lines.size() - 1);
        String[] columns = lastLine.split(";", -1);

        assertEquals("2001", columns[0]);
        assertEquals("Яндекс", columns[1]);
        assertEquals("Санкт-Петербург, пр. Пискаревский, д. 2", columns[3]);
        assertEquals("7736207543", columns[4]);
        assertTrue(columns.length >= 5);
    }

    @Test
    void removeCompanyRecordByINN_removesFromCsv(@TempDir Path tempDir) throws Exception {
        Path csv = createSampleCsv(tempDir);
        CompanyService service = new CompanyService(csv.toString());

        service.removeCompanyRecordByINN("7800000000");

        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        assertTrue(lines.stream().noneMatch(line -> line.split(";", -1).length > 4
                && line.split(";", -1)[4].trim().equals("7800000000")));
        assertThrows(Exception.class, () -> service.findCompanyRecordByINN("7800000000"));
    }

    @Test
    void loadCompanyRecords_skipsInvalidRows(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("companies.csv");
        Files.write(csv, List.of(
                "№;Наименование;Регистрационный номер;Адрес (место нахождения);Код налогоплательщика",
                "1;Компания А;123;Адрес А;7800000000",
                "2;Битая строка;456;Адрес Б;",
                "3;Компания В;789;Адрес В;7811111111"
        ), StandardCharsets.UTF_8);

        CompanyService service = new CompanyService(csv.toString());

        assertEquals("Компания А", service.findCompanyRecordByINN("7800000000").getName());
        assertEquals("Компания В", service.findCompanyRecordByINN("7811111111").getName());
    }

    @Test
    void loadCompanyRecords_skipsDuplicateInnRows(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("companies.csv");
        Files.write(csv, List.of(
                "№;Наименование;Регистрационный номер;Адрес (место нахождения);Код налогоплательщика",
                "1;Компания А;123;Адрес А;7800000000",
                "2;Компания А филиал;456;Адрес А2;7800000000",
                "3;Компания В;789;Адрес В;7811111111"
        ), StandardCharsets.UTF_8);

        CompanyService service = new CompanyService(csv.toString());

        assertEquals("Компания А", service.findCompanyRecordByINN("7800000000").getName());
        assertEquals("Компания В", service.findCompanyRecordByINN("7811111111").getName());
    }
}
