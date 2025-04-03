package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.Parser;
import ru.itmo.infra.storage.IStudentRepo;

import java.io.File;

@Log
public class StudentService {

    private static final Parser excelParser = new Parser();
    private static final Generator excelGenerator = new Generator();
    private static final IStudentRepo studentRepo = null; // TODO: заменить на реализацию

    public File updateStudentsFromExcel(File file, long eduStreamId) throws InternalException, BadRequestException {
        var groups = studentRepo.getAllGroupsByEduStreamID(eduStreamId);
        var groupToStudentDTOsWithErrors = excelParser.parseExcelFile(file, groups);
        if (!groupToStudentDTOsWithErrors.isEmpty()) {
            return excelGenerator.generateExcelWithErrors(groupToStudentDTOsWithErrors);
        }

//        var studentDTOs = studentDTOsWithErrors.getErrorsByRows();

        // TODO: creates students with constructor and update in db

        log.info("файл прошел валидацию!");

        return null;
    }
}
