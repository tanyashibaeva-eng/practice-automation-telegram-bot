package ru.itmo.infra.excel;

import lombok.extern.java.Log;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class Generator {
    private static final String[] headersColumns = {
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

    public static File generateExcelWithErrors(File file, HashMap<String, StudentsWithErrors> errorsByGroups) throws InternalException {
        try (FileInputStream fis = new FileInputStream(file)) {
            var workbook = new XSSFWorkbook(fis);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var studentsWithErrors = errorsByGroups.get(workbook.getSheetName(i));
                if (studentsWithErrors == null) {
                    continue;
                }

                var sheet = workbook.getSheetAt(i);
                var lastColumnIndex = sheet.getRow(0).getPhysicalNumberOfCells();

                var headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    headerRow = sheet.createRow(0);
                }
                var errorHeaderCell = headerRow.createCell(lastColumnIndex);
                errorHeaderCell.setCellValue("Ошибки");

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    var row = sheet.getRow(rowIndex);
                    if (row != null) {
                        List<String> errors = studentsWithErrors.getErrorsByRows().get(rowIndex);
                        String errorMessages = (errors != null) ? String.join("; ", errors) : "";

                        var errorCell = row.createCell(lastColumnIndex);
                        errorCell.setCellValue(errorMessages);
                    }
                }
            }

            file = new File("список студентов – ошибки.xlsx");
            var fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            return file;
        } catch (IOException e) {
            throw new InternalException("Произошла ошибка при обработке Excel файла: " + e.getMessage(), e);
        }
    }

    public static File generateExcel(Map<String, List<Student>> groupToStudents, List<String> groups) throws InternalException {
        var workbook = new XSSFWorkbook();

        var practicePlaceOptions = getPracticePlaceOptions();
        var practiceFormatOptions = getPracticeFormatOptions();

        for (var groupName : groups) {
            if (groupName == null) {
                continue;
            }
            var students = groupToStudents.get(groupName);
            var sheet = workbook.createSheet(groupName);

            var headerRow = sheet.createRow(0);
            for (int i = 0; i < headersColumns.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headersColumns[i]);

                var headerStyle = workbook.createCellStyle();
                var headerFont = workbook.createFont();

                headerFont.setBoldweight((short) 700);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            var rowNum = 1;
            for (Student student : students) {
                var row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(student.getTelegramUser() != null ? student.getTelegramUser().getChatId() + "" : "");
                row.createCell(1).setCellValue(student.getIsu());
                row.createCell(2).setCellValue(groupName);

                var cellFullName = row.createCell(3);
                cellFullName.setCellValue(student.getFullName());
                cellFullName.setCellStyle(createColoredCellStyle(workbook, student.getCellHexColor()));

                row.createCell(4).setCellValue(student.getComments() != null ? student.getStatus().getDisplayName() : "");
                addEnumValidation(sheet, 4, student.getTransitionStatuses(), row.getRowNum(), row.getRowNum());

                row.createCell(5).setCellValue(student.getComments() != null ? student.getComments() : "");
                row.createCell(6).setCellValue(student.getCallStatusComments() != null ? student.getCallStatusComments() : "");
                row.createCell(7).setCellValue(student.getPracticePlace() != null ? student.getPracticePlace().getDisplayName() : "");
                row.createCell(8).setCellValue(student.getPracticeFormat() != null ? student.getPracticeFormat().getDisplayName() : "");
                row.createCell(9).setCellValue(student.getCompanyINN() != null ? student.getCompanyINN() + "" : "");
                row.createCell(10).setCellValue(student.getCompanyName() != null ? student.getCompanyName() : "");
                row.createCell(11).setCellValue(student.getCompanyLeadFullName() != null ? student.getCompanyLeadFullName() : "");
                row.createCell(12).setCellValue(student.getCompanyLeadPhone() != null ? student.getCompanyLeadPhone() : "");
                row.createCell(13).setCellValue(student.getCompanyLeadEmail() != null ? student.getCompanyLeadEmail() : "");
                row.createCell(14).setCellValue(student.getCompanyLeadJobTitle() != null ? student.getCompanyLeadJobTitle() : "");

                for (int i = 0; i < 15; i++) {
                    if (i == 5 || i == 6) {
                        continue;
                    }
                    if (row.getCell(i) == null) {
                        row.getCell(i).setCellStyle(createGrayCellStyle(workbook));
                    }
                    switch (row.getCell(i).getCellType()) {
                        case Cell.CELL_TYPE_BLANK:
                            row.getCell(i).setCellStyle(createGrayCellStyle(workbook));
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            if (row.getCell(i).getCellFormula().isEmpty()) {
                                row.getCell(i).setCellStyle(createGrayCellStyle(workbook));
                            }
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            if (row.getCell(i).getNumericCellValue() == 0) {
                                row.getCell(i).setCellStyle(createGrayCellStyle(workbook));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if (row.getCell(i).getStringCellValue().isEmpty()) {
                                row.getCell(i).setCellStyle(createGrayCellStyle(workbook));
                            }
                            break;
                    }
                }
            }

            applyConditionalFormatting(sheet, sheet.getPhysicalNumberOfRows());

            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 4, 4));

            addEnumValidation(sheet, 7, practicePlaceOptions, 1, sheet.getLastRowNum());
            addEnumValidation(sheet, 8, practiceFormatOptions, 1, sheet.getLastRowNum());

            for (int i = 0; i < headersColumns.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }

        var file = new File("список студентов.xlsx");
        try (var fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
        }

        return file;
    }

    private static CellStyle createColoredCellStyle(Workbook workbook, String hexColor) {
        var style = workbook.createCellStyle();
        if (hexColor != null && !hexColor.isEmpty()) {
            String colorStr = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            byte[] rgb = new byte[]{
                    (byte) Integer.parseInt(colorStr.substring(0, 2), 16),
                    (byte) Integer.parseInt(colorStr.substring(2, 4), 16),
                    (byte) Integer.parseInt(colorStr.substring(4, 6), 16)
            };
            var color = new XSSFColor(rgb);
            ((XSSFCellStyle) style).setFillForegroundColor(color);
            ((XSSFCellStyle) style).setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private static CellStyle createGrayCellStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        var grayColor = new XSSFColor(new byte[]{(byte) 230, (byte) 230, (byte) 230}); // серый
        ((XSSFCellStyle) style).setFillForegroundColor(grayColor);
        ((XSSFCellStyle) style).setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }


    private static void addEnumValidation(Sheet sheet, int colIndex, String[] options, int startRow, int endRow) {
        var dvHelper = sheet.getDataValidationHelper();
        var constraint = dvHelper.createExplicitListConstraint(options);
        var addressList = new CellRangeAddressList(startRow, endRow, colIndex, colIndex);
        var validation = dvHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        sheet.addValidationData(validation);
    }

    private static String[] getPracticePlaceOptions() {
        var values = PracticePlace.values();
        var options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].getDisplayName();
        }
        return options;
    }

    private static String[] getPracticeFormatOptions() {
        var values = PracticeFormat.values();
        var options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].getDisplayName();
        }
        return options;
    }

    private static void applyConditionalFormatting(XSSFSheet sheet, int numberOfRows) {
        for (StudentStatus status : StudentStatus.values()) {
            var rule = sheet.getSheetConditionalFormatting().createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"%s\"".formatted(status.getDisplayName()));
            var pattern = rule.createPatternFormatting();
            pattern.setFillBackgroundColor(HSSFColor.BLUE.index);
            pattern.setFillBackgroundColor(status.getColorForStatus());

            CellRangeAddress[] regions = {CellRangeAddress.valueOf("E2:E" + (numberOfRows + 1))};
            sheet.getSheetConditionalFormatting().addConditionalFormatting(regions, rule);
        }
    }
}
