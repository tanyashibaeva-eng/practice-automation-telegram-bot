package ru.itmo.infra.excel;

import lombok.extern.java.Log;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.StudentsWithErrors;
import ru.itmo.exception.InternalException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Log
public class Generator {
    private static final String[] errorColumns = {
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
            "Должность руководителя",
            "Ошибки"
    };

    public File generateExcelWithErrors(StudentsWithErrors studentsWithErrors) throws InternalException {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Список студентов");

        var headerRow = sheet.createRow(0);
        for (int i = 0; i < errorColumns.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(errorColumns[i]);
        }

        var students = studentsWithErrors.getStudents();
        var errorsByRows = studentsWithErrors.getErrorsByRows();

        int rowNum = 1;
        for (int i = 0; i < students.size(); i++) {
            ExcelStudentDTO student = students.get(i);
            var row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(student.getIsu() == null ? "" : student.getIsu() + "");
            row.createCell(1).setCellValue(student.getStGroup() == null ? "" : student.getStGroup());
            row.createCell(2).setCellValue(student.getFullName() == null ? "" : student.getFullName());
            row.createCell(3).setCellValue(student.getStatus() == null ? "": student.getStatus().toString());
            row.createCell(4).setCellValue(student.getComments() == null ? "" : student.getComments());
            row.createCell(5).setCellValue(student.getCompanyINN() == null ? "": student.getCompanyINN() + "");
            row.createCell(6).setCellValue(student.getCompanyName() == null ? "" : student.getCompanyName());
            row.createCell(7).setCellValue(student.getCompanyLeadFullName() == null ? "" : student.getCompanyLeadFullName());
            row.createCell(8).setCellValue(student.getCompanyLeadPhone() == null ? "" : student.getCompanyLeadPhone());
            row.createCell(9).setCellValue(student.getCompanyLeadEmail() == null ? "" : student.getCompanyLeadEmail());
            row.createCell(10).setCellValue(student.getCompanyLeadJobTitle() == null ? "" : student.getCompanyLeadJobTitle());

            List<String> errors = errorsByRows.get(i + 1);
            String errorMessages = (errors != null) ? String.join("; ", errors) : "";
            row.createCell(11).setCellValue(errorMessages);
        }

        var file = new File("список студентов – ошибки.xlsx");
        try (var fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage(), e);
        }

        return file;
    }

}
