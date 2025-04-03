package ru.itmo.infra.excel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private Parser parser;

    private static final String[] columns = {
            "ИСУ",
            "Группа",
            "ФИО",
            "Статус",
            "Комментарий",
            "ИНН Компании",
            "Компания",
            "Руководитель",
            "Телефон Руководителя",
            "Почта Руководителя",
            "Должность Руководителя"
    };

    @BeforeEach
    void setUp() {
        parser = new Parser();
    }

    @Test
    void testParseExcelFile_ValidFile_ShouldReturnStudentsWithErrors() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < 11; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(123456);
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("REGISTERED");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue(123456789);
        dataRow.createCell(6).setCellValue("Company1");
        dataRow.createCell(7).setCellValue("Jane Doe");
        dataRow.createCell(8).setCellValue("+7 925 123 45 67");
        dataRow.createCell(9).setCellValue("jane.doe@example.com");
        dataRow.createCell(10).setCellValue("Manager");

        var testFile = File.createTempFile("test-file", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var result = parser.parseExcelFile(testFile);

        assertNotNull(result);
        assertEquals(1, result.getStudents().size());
        assertEquals(0, result.getErrorsByRows().size());

        var student = result.getStudents().get(0);
        assertEquals(123456, student.getIsu());
        assertEquals("Group1", student.getStGroup());
        assertEquals("John Doe", student.getFullName());
        assertEquals(StudentStatus.REGISTERED, student.getStatus());
        assertEquals("No comment", student.getComments());
        assertEquals(123456789, student.getCompanyINN());
        assertEquals("Company1", student.getCompanyName());
        assertEquals("Jane Doe", student.getCompanyLeadFullName());
        assertEquals("+7 925 123 45 67", student.getCompanyLeadPhone());
        assertEquals("jane.doe@example.com", student.getCompanyLeadEmail());
        assertEquals("Manager", student.getCompanyLeadJobTitle());
    }

    @Test
    void testParseExcelFile_InvalidTemplate_ShouldThrowBadRequestException() {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Incorrect Header");

        var testFile = new File("invalid-template.xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var exception = assertThrows(BadRequestException.class, () -> {
            parser.parseExcelFile(testFile);
        });

        assertEquals("Неверный шаблон загружаемого файла", exception.getMessage());
    }

    @Test
    void testParseExcelFile_MissingRequiredColumn_ShouldReturnErrors() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < 11; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("");
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("A");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue(123456789);
        dataRow.createCell(6).setCellValue("Company1");
        dataRow.createCell(7).setCellValue("Jane Doe");
        dataRow.createCell(8).setCellValue("+7 (925) 123-45-67");
        dataRow.createCell(9).setCellValue("jane.doe@example.com");
        dataRow.createCell(10).setCellValue("Manager");

        var testFile = File.createTempFile("test-file-with-errors", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var result = parser.parseExcelFile(testFile);
        assertNotNull(result);

        assertEquals(1, result.getErrorsByRows().size());
        var errors = result.getErrorsByRows().get(1);
        assertTrue(errors.contains("значение в колонке \"ИСУ\" должно быть числом"));
    }

    @AfterAll
    static void tearDown() throws IOException {
        var filePath = Paths.get("./test-file-with-errors.xlsx");
        Files.deleteIfExists(filePath);

        filePath = Paths.get("./invalid-template.xlsx");
        Files.deleteIfExists(filePath);

        filePath = Paths.get("./test-file.xlsx");
        Files.deleteIfExists(filePath);
    }
}
