package ru.itmo.infra.excel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private static final String[] columns = {
            "chatID",
            "ИСУ",
            "Группа",
            "ФИО",
            "Статус",
            "Комментарий",
            "Комментарий по звонкам руководителю",
            "Место практики",
            "Формат практики",
            "ИНН Компании",
            "Компания",
            "Руководитель",
            "Телефон Руководителя",
            "Почта Руководителя",
            "Должность Руководителя"
    };

    @AfterAll
    static void tearDown() throws IOException {
        var filePath = Paths.get("./test-file-with-errors.xlsx");
        Files.deleteIfExists(filePath);

        filePath = Paths.get("./invalid-template.xlsx");
        Files.deleteIfExists(filePath);

        filePath = Paths.get("./test-file.xlsx");
        Files.deleteIfExists(filePath);
    }

    @Test
    void testParseUpdateExcelFile_ValidFile_ShouldReturnStudentsWithErrors() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < columns.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(123456);
        dataRow.createCell(1).setCellValue(123456);
        dataRow.createCell(2).setCellValue("Group1");
        dataRow.createCell(3).setCellValue("John Doe");
        dataRow.createCell(4).setCellValue("Зарегистрирован");
        dataRow.createCell(5).setCellValue("No comment");
        dataRow.createCell(6).setCellValue("didn't answer");
        dataRow.createCell(7).setCellValue("Практика в ИТМО");
        dataRow.createCell(8).setCellValue("Онлайн");
        dataRow.createCell(9).setCellValue(123456789);
        dataRow.createCell(10).setCellValue("Company1");
        dataRow.createCell(11).setCellValue("Jane Doe");
        dataRow.createCell(12).setCellValue("+7 925 123 45 67");
        dataRow.createCell(13).setCellValue("jane.doe@example.com");
        dataRow.createCell(14).setCellValue("Manager");

        var testFile = File.createTempFile("test-file", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var mapResult = Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        assertNotNull(mapResult);
        var result = mapResult.get("gr1");

        assertEquals(1, result.getStudents().size());
        assertEquals(0, result.getErrorsByRows().size());

        var student = result.getStudents().get(0);
        assertEquals(123456, student.getIsu());
        assertEquals("Group1", student.getStGroup());
        assertEquals("John Doe", student.getFullName());
        assertEquals(StudentStatus.REGISTERED, student.getStatus());
        assertEquals("No comment", student.getComments());
        assertEquals("didn't answer", student.getCallStatusComments());
        assertEquals(PracticePlace.ITMO_MARKINA, student.getPracticePlace());
        assertEquals(PracticeFormat.NOT_SPECIFIED, student.getPracticeFormat());
        assertEquals(123456789, student.getCompanyINN());
        assertEquals("Company1", student.getCompanyName());
        assertEquals("Jane Doe", student.getCompanyLeadFullName());
        assertEquals("+7 925 123 45 67", student.getCompanyLeadPhone());
        assertEquals("jane.doe@example.com", student.getCompanyLeadEmail());
        assertEquals("Manager", student.getCompanyLeadJobTitle());
    }

    @Test
    void testParseUpdateExcelFile_InvalidTemplate_ShouldThrowBadRequestException() {
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
            Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        });

        assertEquals("Неверный шаблон загружаемого файла", exception.getMessage());
    }

    @Test
    void testParseUpdateExcelFile_MissingRequiredColumn_ShouldReturnErrors() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("");
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("A");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue("didn't answer");
        dataRow.createCell(6).setCellValue("ITMO_MARKINA");
        dataRow.createCell(7).setCellValue("NOT_SPECIFIED");
        dataRow.createCell(8).setCellValue(123456789);
        dataRow.createCell(9).setCellValue("Company1");
        dataRow.createCell(10).setCellValue("Jane Doe");
        dataRow.createCell(11).setCellValue("+7 (925) 123-45-67");
        dataRow.createCell(12).setCellValue("jane.doe@example.com");
        dataRow.createCell(13).setCellValue("Manager");

        var testFile = File.createTempFile("test-file-with-errors", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var mapResult = Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        assertNotNull(mapResult);
        var result = mapResult.get("gr1");

        assertEquals(1, result.getErrorsByRows().size());
        var errors = result.getErrorsByRows().get(1);
        assertTrue(errors.contains("значение в колонке \"ИСУ\" должно быть числом"));
    }

    @Test
    void testParseUpdateExcelFile_FileWithEmptyValues_ShouldReturnErrors() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("");
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("Зарегистрирован");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue("didn't answer");
        dataRow.createCell(6).setCellValue("Практика в ИТМО");
        dataRow.createCell(8).setCellValue(123456789);
        dataRow.createCell(9).setCellValue("Company1");
        dataRow.createCell(10).setCellValue("Jane Doe");
        dataRow.createCell(11).setCellValue("+7 925 123 45 67");
        dataRow.createCell(12).setCellValue("jane.doe@example.com");
        dataRow.createCell(13).setCellValue("Manager");

        var testFile = File.createTempFile("test-file-with-empty-values", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var mapResult = Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        assertNotNull(mapResult);
        var result = mapResult.get("gr1");

        assertEquals(1, result.getErrorsByRows().size());
        var errors = result.getErrorsByRows().get(1);
        assertTrue(errors.contains("значение в колонке \"ИСУ\" должно быть числом"));
    }

    @Test
    void testParseUpdateExcelFile_InvalidStatus_ShouldReturnError() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(123456);
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("InvalidStatus");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue("didn't answer");
        dataRow.createCell(6).setCellValue("Практика в ИТМО");
        dataRow.createCell(8).setCellValue(123456789);
        dataRow.createCell(9).setCellValue("Company1");
        dataRow.createCell(10).setCellValue("Jane Doe");
        dataRow.createCell(11).setCellValue("+7 925 123 45 67");
        dataRow.createCell(12).setCellValue("jane.doe@example.com");
        dataRow.createCell(13).setCellValue("Manager");

        var testFile = File.createTempFile("test-file-invalid-status", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var mapResult = Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        assertNotNull(mapResult);
        var result = mapResult.get("gr1");

        assertEquals(1, result.getErrorsByRows().size());
        var errors = result.getErrorsByRows().get(1);
        assertTrue(errors.contains("значение в колонке \"Статус\" может быть одним из \"Не зарегистрирован\", \"Зарегистрирован\", \"Практика в ИТМО у Маркиной Т. А.\", \"Данные о компании на проверке\", \"Данные о компании возвращены на доработку\", \"Практика в ИТМО\", \"Данные о компании утверждены и ожидается заполнения заявки\", \"Заявка на проверке\", \"Заявка возвращена на доработку\", \"Заявка согласована и ожидает подписания\", \"Заявка подписана\""));
    }

    @Test
    void testParseUpdateExcelFile_InvalidPhone_ShouldReturnError() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        var headerRow = sheet.createRow(0);
        for (var i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        var dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(123456);
        dataRow.createCell(1).setCellValue("Group1");
        dataRow.createCell(2).setCellValue("John Doe");
        dataRow.createCell(3).setCellValue("Зарегистрирован");
        dataRow.createCell(4).setCellValue("No comment");
        dataRow.createCell(5).setCellValue("didn't answer");
        dataRow.createCell(6).setCellValue("Практика в ИТМО");
        dataRow.createCell(8).setCellValue(123456789);
        dataRow.createCell(9).setCellValue("Company1");
        dataRow.createCell(10).setCellValue("Jane Doe");
        dataRow.createCell(11).setCellValue("12345");
        dataRow.createCell(12).setCellValue("jane.doe@example.com");
        dataRow.createCell(13).setCellValue("Manager");

        var testFile = File.createTempFile("test-file-invalid-phone", ".xlsx");
        try (var fos = new FileOutputStream(testFile)) {
            workbook.write(fos);
        }

        var mapResult = Parser.parseUpdateExcelFile(testFile, List.of("gr1"));
        assertNotNull(mapResult);
        var result = mapResult.get("gr1");

        assertEquals(1, result.getErrorsByRows().size());
        var errors = result.getErrorsByRows().get(1);
        assertTrue(errors.contains("значение в колонке \"Телефон Руководителя\" должно быть номером телефона (+7 925 123 45 67)"));
    }
}
