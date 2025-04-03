package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.Parser;

import java.io.File;

@Log
public class StudentService {

    private static final Parser excelParser = new Parser();
    private static final Generator excelGenerator = new Generator();

    public File updateStudentsFromExcel(File file) throws InternalException, BadRequestException {
        var studentDTOsWithErrors = excelParser.parseExcelFile(file);
        if (!studentDTOsWithErrors.getErrorsByRows().isEmpty()) {
            return excelGenerator.generateExcelWithErrors(studentDTOsWithErrors);
        }

        var studentDTOs = studentDTOsWithErrors.getErrorsByRows();

        // TODO: creates students with constructor and update in db

        log.info("файл прошел валидацию!");

        return null;
    }
}
