package ru.itmo.infra.excel;

import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.util.TextParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Log
public class Parser {
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

    private static final TextParser textParser = new TextParser();

    public StudentsWithErrors parseExcelFile(File file) throws BadRequestException, InternalException {
        try (FileInputStream fis = new FileInputStream(file)) {
            var workbook = new XSSFWorkbook(fis);
            var sheet = workbook.getSheetAt(0);

            var headers = sheet.getRow(0);
            checkTemplate(headers.cellIterator());

            var rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();
            return parseStudents(rowIterator);
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
        }
    }

    private static void checkTemplate(Iterator<Cell> headersIterator) throws BadRequestException {
        var invalidTemplateException = new BadRequestException("Неверный шаблон загружаемого файла");
        try {
            var columnCount = 0;
            while (headersIterator.hasNext()) {
                var header = headersIterator.next().getStringCellValue();
                if (columnCount >= columns.length) {
                    break;
                }
                if (!header.equals(columns[columnCount])) {
                    throw invalidTemplateException;
                }
                columnCount++;
            }

            if (columnCount != columns.length) {
                throw invalidTemplateException;
            }
        } catch (Exception e) {
            throw invalidTemplateException;
        }
    }

    private static StudentsWithErrors parseStudents(Iterator<Row> rowIterator) throws InternalException {
        var students = new ArrayList<ExcelStudentDTO>();
        var errorsByRows = new HashMap<Integer, List<String>>();

        while (rowIterator.hasNext()) {
            var row = rowIterator.next();
            if (row == null) continue;

            for (var i = 0; i < 4; i++) {
                if (row.getCell(i) == null) {
                    addErr(row.getRowNum(), "поле %s является обязательным".formatted(columns[i+1]), errorsByRows);
                }
            }

            try {
                var isu = (parseInt(row.getCell(0), errorsByRows, false));
                var group = (parseString(row.getCell(1), errorsByRows, false));
                var fullName = (parseString(row.getCell(2), errorsByRows, false));
                var status = (parseStatus(row.getCell(3), errorsByRows));
                var comment = (parseString(row.getCell(4), errorsByRows, true));
                var companyINN = (parseInt(row.getCell(5), errorsByRows, true));
                var companyName = (parseString(row.getCell(6), errorsByRows, true));
                var leadFullName = (parseString(row.getCell(7), errorsByRows, true));
                var leadPhone = (parsePhone(row.getCell(8), errorsByRows, true));
                var leadEmail = (parseEmail(row.getCell(9), errorsByRows, true));
                var leadPos = (parseString(row.getCell(10), errorsByRows, true));

                var studentDTO = new ExcelStudentDTO(
                        isu,
                        group,
                        fullName,
                        status, comment,
                        companyINN,
                        companyName,
                        leadFullName,
                        leadPhone,
                        leadEmail,
                        leadPos
                );
                students.add(studentDTO);
            } catch (Exception e) {
                throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
            }
        }

        return new StudentsWithErrors(students, errorsByRows);
    }

    private static String parseString(Cell cell, HashMap<Integer, List<String>> errorsByRows, boolean canBeEmpty) throws InternalException {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue() + "";
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BLANK:
                if (canBeEmpty) {
                    return null;
                }
                addErr(cell.getRowIndex(), "поле %s является обязательным".formatted(columns[cell.getColumnIndex()]), errorsByRows);
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() + "";
            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue() + "";
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                addErr(cell.getRowIndex(), "неверный тип ячейки: %s".formatted(cell.getCellType()), errorsByRows);
                return "";
        }
    }

    private static Integer parseInt(Cell cell, HashMap<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if ((canBeEmpty && strVal == null) || (!canBeEmpty && strVal == null)) {
                return null;
            }
            return textParser.parseDoubleToInt(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть числом".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return 0;
    }

    private static String parsePhone(Cell cell, HashMap<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if ((canBeEmpty && strVal == null) || (!canBeEmpty && strVal == null)) {
                return null;
            }
            return textParser.parsePhone(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть номером телефона (+7 925 123 45 67)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return "";
    }

    private static String parseEmail(Cell cell, HashMap<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if ((canBeEmpty && strVal == null) || (!canBeEmpty && strVal == null)) {
                return null;
            }
            return textParser.parseEmail(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть электронной почтой (ivanov@yandex.ru)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return "";
    }

    private static StudentStatus parseStatus(Cell cell, HashMap<Integer, List<String>> errorsByRows) {
        try {
            var strVal = parseString(cell, errorsByRows, false);
            if (strVal == null) {
                return null;
            }
            return textParser.parseStatus(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из [A, B, C]".formatted(columns[cell.getColumnIndex()]), errorsByRows); // TODO: сделать статусы
        }
        return null;
    }

    private static void addErr(int row, String text, HashMap<Integer, List<String>> errorsByRows) {
        if (!errorsByRows.containsKey(row)) {
            errorsByRows.put(row, new ArrayList<>());
        }
        errorsByRows.get(row).add(text);
    }
}
