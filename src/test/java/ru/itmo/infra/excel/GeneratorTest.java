package ru.itmo.infra.excel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.exception.InternalException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTest {

    private Generator generator;

    @BeforeEach
    void setUp() {
        generator = new Generator();
    }

    @Test
    void testGenerateExcelWithErrors() throws InternalException, IOException {
        var students = Arrays.asList(
                new ExcelStudentDTO(1, "group1", "John Doe", null, "comment", "called", PracticePlace.ITMO_MARKINA, PracticeFormat.ONLINE, 12345, "Company1", "Lead1", "+7 123 456 78 90", "lead1@example.com", "Manager", "FFFFFF"),
                new ExcelStudentDTO(2, "group2", "Jane Doe", null, "comment2", "not answering", PracticePlace.ITMO_UNIVERSITY, PracticeFormat.HYBRID, 67890, "Company2", "Lead2", "+7 987 654 32 10", "lead2@example.com", "Director", "FFFFFF")
        );

        var errors = new HashMap<Integer, List<String>>();
        errors.put(1, Arrays.asList("Invalid phone number", "Invalid email address"));
        errors.put(2, new ArrayList<>());


        var file = generator.generateExcelWithErrors(new StudentsWithErrors(students, errors));

        assertTrue(file.exists());

        try (var fis = new FileInputStream(file)) {
            var workbook = new XSSFWorkbook(fis);

            var sheet = workbook.getSheetAt(0);
            var headerRow = sheet.getRow(0);

            assertEquals("ИСУ", headerRow.getCell(0).getStringCellValue());
            assertEquals("Группа", headerRow.getCell(1).getStringCellValue());
            assertEquals("ФИО", headerRow.getCell(2).getStringCellValue());
            assertEquals("Статус", headerRow.getCell(3).getStringCellValue());
            assertEquals("Комментарий", headerRow.getCell(4).getStringCellValue());
            assertEquals("ИНН Компании", headerRow.getCell(5).getStringCellValue());
            assertEquals("Компания", headerRow.getCell(6).getStringCellValue());
            assertEquals("Руководитель", headerRow.getCell(7).getStringCellValue());
            assertEquals("Телефон Руководителя", headerRow.getCell(8).getStringCellValue());
            assertEquals("Почта Руководителя", headerRow.getCell(9).getStringCellValue());
            assertEquals("Должность руководителя", headerRow.getCell(10).getStringCellValue());
            assertEquals("Ошибки", headerRow.getCell(11).getStringCellValue());

            var dataRow1 = sheet.getRow(1);
            assertEquals("1", dataRow1.getCell(0).getStringCellValue());
            assertEquals("group1", dataRow1.getCell(1).getStringCellValue());
            assertEquals("John Doe", dataRow1.getCell(2).getStringCellValue());
            assertEquals("comment", dataRow1.getCell(4).getStringCellValue());
            assertEquals("12345", dataRow1.getCell(5).getStringCellValue());
            assertEquals("Company1", dataRow1.getCell(6).getStringCellValue());
            assertEquals("Lead1", dataRow1.getCell(7).getStringCellValue());
            assertEquals("+7 123 456 78 90", dataRow1.getCell(8).getStringCellValue());
            assertEquals("lead1@example.com", dataRow1.getCell(9).getStringCellValue());
            assertEquals("Manager", dataRow1.getCell(10).getStringCellValue());
            assertEquals("Invalid phone number; Invalid email address", dataRow1.getCell(11).getStringCellValue());

            var dataRow2 = sheet.getRow(2);
            assertEquals("2", dataRow2.getCell(0).getStringCellValue());
            assertEquals("group2", dataRow2.getCell(1).getStringCellValue());
            assertEquals("Jane Doe", dataRow2.getCell(2).getStringCellValue());
            assertEquals("comment2", dataRow2.getCell(4).getStringCellValue());
            assertEquals("67890", dataRow2.getCell(5).getStringCellValue());
            assertEquals("Company2", dataRow2.getCell(6).getStringCellValue());
            assertEquals("Lead2", dataRow2.getCell(7).getStringCellValue());
            assertEquals("+7 987 654 32 10", dataRow2.getCell(8).getStringCellValue());
            assertEquals("lead2@example.com", dataRow2.getCell(9).getStringCellValue());
            assertEquals("Director", dataRow2.getCell(10).getStringCellValue());
            assertEquals("", dataRow2.getCell(11).getStringCellValue());
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        var filePath = Paths.get("./список студентов – ошибки.xlsx");
        Files.deleteIfExists(filePath);
    }
}
