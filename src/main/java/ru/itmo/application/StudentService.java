package ru.itmo.application;

import lombok.extern.java.Log;
import org.checkerframework.checker.units.qual.A;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.model.Student;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.Parser;
import ru.itmo.infra.storage.EduStreamRepository;
import ru.itmo.infra.storage.Filter;
import ru.itmo.infra.storage.StudentRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Log
public class StudentService {

    private static final Parser excelParser = new Parser();
    private static final Generator excelGenerator = new Generator();

    public static Optional<File> updateStudentsFromExcel(File file, long eduStreamId) throws InternalException, BadRequestException {
        var groups = List.of("gr1"); // TODO: replace
        var students = StudentRepository.findAll(Filter.builder().eduStreamId(eduStreamId).build());
        var groupToStudentDTOsWithErrors = excelParser.parseExcelFile(file, groups);

        var studentInfoToStudents = new HashMap<String, ExcelStudentDTO>();
        for (var g : groups) {
            var dtos = groupToStudentDTOsWithErrors.get(g).getStudents();
            var errors = groupToStudentDTOsWithErrors.get(g).getErrorsByRows();

            if (!errors.isEmpty()) {
                return Optional.ofNullable(excelGenerator.generateExcelWithErrors(groupToStudentDTOsWithErrors));
            }

            for (var d : dtos) {
                studentInfoToStudents.put("%d-%s-%s".formatted(d.getIsu(), d.getFullName(), d.getStGroup()), d);
            }
        }
        var haveErrors = false;
        for (var s : students) {
            var key = "%s-%s-%s".formatted(s.getIsu(), s.getFullName(), s.getStGroup());
            if (studentInfoToStudents.containsKey(key)) {
                var d = studentInfoToStudents.get(key);
                var errors = s.updateOrGetErrors(d);
                if (!errors.isEmpty()) {
                    haveErrors = true;
                    groupToStudentDTOsWithErrors.get(s.getStGroup()).getErrorsByRows().put(d.getIsu(), errors);
                }
            }
        }
        if (haveErrors) {
            return Optional.ofNullable(excelGenerator.generateExcelWithErrors(groupToStudentDTOsWithErrors));
        }

        StudentRepository.saveBatch(students);
        return Optional.empty();
    }

    public static File exportStudentsToExcel(long eduStreamId) throws InternalException {
        var groups = EduStreamRepository.findAllGroupsByStreamId(eduStreamId);
        var students = StudentRepository.findAll(Filter.builder().eduStreamId(eduStreamId).build());
        var groupToStudents = new HashMap<String, List<Student>>();

        for (var s : students) {
            if (!groupToStudents.containsKey(s.getStGroup())) {
                groupToStudents.put(s.getStGroup(), new ArrayList<>());
            }
            groupToStudents.get(s.getStGroup()).add(s);
        }

        return excelGenerator.generateExcel(groupToStudents, groups);
    }
}
