package ru.itmo.infra.excel;

import lombok.extern.java.Log;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.util.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log
public class Parser {
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

    private static final BadRequestException invalidTemplateException = new BadRequestException("Неверный шаблон загружаемого файла");

    public static HashMap<String, StudentsWithErrors> parseUpdateExcelFile(File file, List<String> groups) throws BadRequestException, InternalException {
        try (FileInputStream fis = new FileInputStream(file)) {
            var workbook = getWorkbook(fis);

            if (workbook.getNumberOfSheets() != groups.size()) {
                throw invalidTemplateException;
            }

            var groupToErrors = new HashMap<String, StudentsWithErrors>();
            for (var sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                var sheet = workbook.getSheetAt(sheetIndex);
                var headers = sheet.getRow(0);
                checkUpdateTemplate(headers.cellIterator());

                var rowIterator = sheet.iterator();
                if (rowIterator.hasNext()) rowIterator.next();
                groupToErrors.put(groups.get(sheetIndex), parseUpdateStudents(rowIterator));
            }

            return groupToErrors;
        } catch (NullPointerException e) {
            log.severe(e.getMessage());
            throw new BadRequestException("Неверный шаблон загружаемого файла (файл пустой)");
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
        }
    }

    private static void checkUpdateTemplate(Iterator<Cell> headersIterator) throws BadRequestException {
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

    private static StudentsWithErrors parseUpdateStudents(Iterator<Row> rowIterator) throws InternalException {
        var students = new ArrayList<ExcelStudentDTO>();
        var errorsByRows = new HashMap<Integer, List<String>>();

        while (rowIterator.hasNext()) {
            var row = rowIterator.next();
            if (row == null) continue;

            var count = 0;
            for (int i : new int[]{1, 2, 3, 4}) {
                if (row.getCell(i) == null || parseString(row.getCell(i), errorsByRows, false) == null) {
                    count++;
                    addErr(row.getRowNum(), "поле %s является обязательным".formatted(columns[i + 1]), errorsByRows);
                }
            }
            if (count == 4) {
                errorsByRows.remove(row.getRowNum());
                continue;
            }

            try {
                var chatId = parseLong(row.getCell(0), errorsByRows, true);
                var isu = parseInt(row.getCell(1), errorsByRows, false);
                var group = parseString(row.getCell(2), errorsByRows, false);
                var fullName = parseString(row.getCell(3), errorsByRows, false);
                var status = parseStatus(row.getCell(4), errorsByRows);
                var comment = parseString(row.getCell(5), errorsByRows, true);
                var callStatusComments = parseString(row.getCell(6), errorsByRows, true);
                var practicePlace = parsePracticePlace(row.getCell(7), errorsByRows);
                var practiceFormat = parsePracticeFormat(row.getCell(8), errorsByRows);
                var companyINN = parseLong(row.getCell(9), errorsByRows, true);
                var companyName = parseString(row.getCell(10), errorsByRows, true);
                var leadFullName = parseString(row.getCell(11), errorsByRows, true);
                var leadPhone = parsePhone(row.getCell(12), errorsByRows, true);
                var leadEmail = parseEmail(row.getCell(13), errorsByRows, true);
                var leadJobTitle = parseString(row.getCell(14), errorsByRows, true);
                var cellHexColor = parseCellColor(row.getCell(3));

                if (cellHexColor.isEmpty() || cellHexColor.equals("0") || cellHexColor.equals("000000")) {
                    cellHexColor = "FFFFFF";
                }

                var studentDTO = new ExcelStudentDTO(
                        chatId,
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
                        cellHexColor,
                        row.getRowNum()
                );
                students.add(studentDTO);
            } catch (Exception e) {
                throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
            }
        }

        return new StudentsWithErrors(students, errorsByRows);
    }

    public static String parseCellColor(Cell cell) {
        if (cell == null) {
            return "FFFFFF";
        }

        CellStyle cellStyle = cell.getCellStyle();
        short colorIndex = cellStyle.getFillForegroundColor();
        if (colorIndex == IndexedColors.AUTOMATIC.getIndex()) {
            return "FFFFFF";
        }

        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
        var rgb = xssfCellStyle.getFillForegroundColorColor().getRgb();
        return String.format("#%02X%02X%02X", rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
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
            return TextUtils.parseDoubleToInt(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть числом".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static Long parseLong(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null || strVal.isEmpty()) {
                return null;
            }
            return TextUtils.parseDoubleStrToLong(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть числом".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static String parsePhone(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null || strVal.isEmpty()) {
                return null;
            }
            String phoneStr = "";
            if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                phoneStr = String.valueOf((long) cell.getNumericCellValue());
            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                phoneStr = cell.getStringCellValue();
            } else {
                phoneStr = cell.getStringCellValue();
            }
            return TextUtils.parsePhone(phoneStr);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть номером телефона (+7 925 123 45 67)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static String parseEmail(Cell cell, Map<Integer, List<String>> errorsByRows, boolean canBeEmpty) {
        try {
            var strVal = parseString(cell, errorsByRows, canBeEmpty);
            if (strVal == null || strVal.isEmpty()) {
                return null;
            }
            return TextUtils.parseEmail(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" должно быть электронной почтой (ivanov@yandex.ru)".formatted(columns[cell.getColumnIndex()]), errorsByRows);
        }
        return null;
    }

    private static StudentStatus parseStatus(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            if (cell == null) {
                return StudentStatus.NOT_REGISTERED;
            }
            var strVal = parseString(cell, errorsByRows, true);
            if (strVal == null) {
                return StudentStatus.NOT_REGISTERED;
            }
            return TextUtils.parseStatusByDisplayName(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из %s".formatted(columns[cell.getColumnIndex()], getStatusEnumNames()), errorsByRows);
        }
        return StudentStatus.NOT_REGISTERED;
    }

    private static PracticeFormat parsePracticeFormat(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            if (cell == null) {
                return PracticeFormat.NOT_SPECIFIED;
            }
            var strVal = parseString(cell, errorsByRows, true);
            if (strVal == null || strVal.isEmpty())
                return PracticeFormat.NOT_SPECIFIED;
            return TextUtils.parsePracticeFormatByDisplayName(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из %s".formatted(columns[cell.getColumnIndex()], getPracticeFormatEnumNames()), errorsByRows);
        }
        return PracticeFormat.NOT_SPECIFIED;
    }

    private static PracticePlace parsePracticePlace(Cell cell, Map<Integer, List<String>> errorsByRows) {
        try {
            if (cell == null) {
                return PracticePlace.NOT_SPECIFIED;
            }
            var strVal = parseString(cell, errorsByRows, true);
            if (strVal == null || strVal.isEmpty())
                return PracticePlace.NOT_SPECIFIED;
            return TextUtils.parsePracticePlaceByDisplayName(strVal);
        } catch (Exception e) {
            addErr(cell.getRowIndex(), "значение в колонке \"%s\" может быть одним из %s".formatted(columns[cell.getColumnIndex()], getPracticePlaceEnumNames()), errorsByRows);
        }
        return PracticePlace.NOT_SPECIFIED;
    }

    private static void addErr(int row, String text, Map<Integer, List<String>> errorsByRows) {
        if (!errorsByRows.containsKey(row)) {
            errorsByRows.put(row, new ArrayList<>());
        }
        errorsByRows.get(row).add(text);
    }

    private static String getStatusEnumNames() {
        return Arrays.stream(StudentStatus.values()).map(StudentStatus::getDisplayName).map(str -> "\"" + str + "\"").collect(Collectors.joining(", "));
    }

    private static String getPracticeFormatEnumNames() {
        return Arrays.stream(PracticeFormat.values()).map(PracticeFormat::getDisplayName).map(str -> "\"" + str + "\"").collect(Collectors.joining(", "));
    }

    private static String getPracticePlaceEnumNames() {
        return Arrays.stream(PracticePlace.values()).map(PracticePlace::getDisplayName).map(str -> "\"" + str + "\"").collect(Collectors.joining(", "));
    }

    private static Workbook getWorkbook(FileInputStream fis) throws BadRequestException, IOException {
        try {
            var workbook = new XSSFWorkbook(fis);
            return workbook;
        } catch (Exception e) {
            try {
                var workbook = new HSSFWorkbook(fis);
                return workbook;
            } catch (Exception e1) {
                throw new BadRequestException("Неподдерживаемый формат файла");
            }
        }
    }
}
