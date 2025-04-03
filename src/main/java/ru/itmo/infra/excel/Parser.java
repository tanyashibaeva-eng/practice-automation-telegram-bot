package ru.itmo.infra.excel;

import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
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

            for (int i : new int[]{0, 1, 2, 3, 6, 7}) {
                if (row.getCell(i) == null) {
                    addErr(row.getRowNum(), "поле %s является обязательным".formatted(columns[i + 1]), errorsByRows);
                }
            }

            try {
                var isu = parseInt(row.getCell(0), errorsByRows, false);
                var group = parseString(row.getCell(1), errorsByRows, false);
                var fullName = parseString(row.getCell(2), errorsByRows, false);
                var status = parseStatus(row.getCell(3), errorsByRows);
                var comment = parseString(row.getCell(4), errorsByRows, true);
                var callStatusComments = parseString(row.getCell(5), errorsByRows, true);
                var practicePlace = parsePracticePlace(row.getCell(6), errorsByRows);
                var practiceFormat = parsePracticeFormat(row.getCell(7), errorsByRows);
                var companyINN = parseInt(row.getCell(8), errorsByRows, true);
                var companyName = parseString(row.getCell(9), errorsByRows, true);
                var leadFullName = parseString(row.getCell(10), errorsByRows, true);
                var leadPhone = parsePhone(row.getCell(11), errorsByRows, true);
                var leadEmail = parseEmail(row.getCell(12), errorsByRows, true);
                var leadJobTitle = parseString(row.getCell(13), errorsByRows, true);
                var cellHexColor = parseString(row.getCell(14), errorsByRows, false);

                var studentDTO = new ExcelStudentDTO(
                        isu,
                        group,
                        fullName,
                        status,
                        comment,
                        callStatusComments,
                        practicePlace,
                        practiceFormat,
                        companyINN,
                        companyName,
                        leadFullName,
                        leadPhone,
                        leadEmail,
                        leadJobTitle,
                        cellHexColor
                );
                students.add(studentDTO);
            } catch (Exception e) {
                throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
            }
        }

        return new StudentsWithErrors(students, errorsByRows);
    }

    private static String parseString(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC -> cell.getNumericCellValue() + "";
            case Cell.CELL_TYPE_STRING -> cell.getStringCellValue();
            case Cell.CELL_TYPE_BLANK -> {
                if (!canBeEmpty)
                    addErr(cell.getRowIndex(), "поле %s является обязательным".formatted(columns[cell.getColumnIndex()]), errorsByRows);
                yield null;
            }
            case Cell.CELL_TYPE_BOOLEAN -> cell.getBooleanCellValue() + "";
            case Cell.CELL_TYPE_ERROR -> cell.getErrorCellValue() + "";
            case Cell.CELL_TYPE_FORMULA -> cell.getCellFormula();
            default -> {
                addErr(cell.getRowIndex(), "неверный тип ячейки: %s".formatted(cell.getCellType()), errorsByRows);
                yield null;
            }
        };
    }

    private static Integer parseInt(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null) {
                return null;
            }
            return textParser.parseDoubleToInt(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть числом".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static String parsePhone(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null) {
                return null;
            }
            return textParser.parsePhone(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть номером телефона (+7 925 123 45 67)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static String parseEmail(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null) {
                return null;
            }
            return textParser.parseEmail(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть электронной почтой (ivanov@yandex.ru)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static StudentStatus parseStatus(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            var strVal = parseString(cell, errorsByRows, false);
            if (strVal == null) {
                return null;
            }
            return textParser.parseStatus(strVal);
        } catch (Exception e) {
            // TODO: сделать статусы
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из [A, B, C]".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static PracticeFormat parsePracticeFormat(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            var strVal = parseString(cell, errorsByRows, false);
            if (strVal == null)
                return null;
            return textParser.parsePracticeFormat(strVal);
        } catch (Exception e) {
            // TODO: сделать форматы прохождения практики
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из [A, B, C]".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static PracticePlace parsePracticePlace(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            var strVal = parseString(cell, errorsByRows, false);
            if (strVal == null)
                return null;
            return textParser.parsePracticePlace(strVal);
        } catch (Exception e) {
            // TODO: сделать места прохождения практики
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из [A, B, C]".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static void addErr(int row, String text, Map<Integer, List<String>> errorsByRows) {
        if (!errorsByRows.containsKey(row)) {
            errorsByRows.put(row, new ArrayList<>());
        }
        errorsByRows.get(row).add(text);
    }
}
