package ru.itmo.application;

import lombok.extern.java.Log;
import org.checkerframework.checker.units.qual.C;
import ru.itmo.domain.dto.CreateStudentDTO;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.ExcelStudentInfoDTO;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
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

    public static Optional<File> updateStudentsFromExcel(File file, long eduStreamId) throws InternalException, BadRequestException {
        var groups = EduStreamRepository.findAllGroupsByStreamId(eduStreamId);
        var students = StudentRepository.findAll(Filter.builder().eduStreamId(eduStreamId).build());
        var groupToStudentDTOsWithErrors = Parser.parseUpdateExcelFile(file, groups);

        var studentInfoToStudents = new HashMap<String, ExcelStudentDTO>();
        for (var g : groups) {
            var dtos = groupToStudentDTOsWithErrors.get(g).getStudents();
            var errors = groupToStudentDTOsWithErrors.get(g).getErrorsByRows();

            if (!errors.isEmpty()) {
                return Optional.of(Generator.generateExcelWithErrors(file, groupToStudentDTOsWithErrors));
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
                    groupToStudentDTOsWithErrors.get(s.getStGroup()).getErrorsByRows().put(d.getRow(), errors);
                }
            }
        }

        if (haveErrors) {
            return Optional.of(Generator.generateExcelWithErrors(file, groupToStudentDTOsWithErrors));
        }

        StudentRepository.updateBatchByChatIdAndEduStreamId(students);
        return Optional.empty();
    }

    public static Optional<File> createStudentsFromExcel(File file, String eduStreamName) throws InternalException, BadRequestException {
        var parsedStudents = Parser.parseCreateExcelFile(file);
        var studentsToCreate = new ArrayList<CreateStudentDTO>();
        for (var s : parsedStudents) {
            if (!s.getErrors().isEmpty()) {
                return Optional.of(Generator.generateExcelCreateWithErrors(file, parsedStudents));
            }
            studentsToCreate.add(new CreateStudentDTO(eduStreamName, s.getGroup(), s.getIsu(), s.getFullName()));
        }

        // TODO: add students

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

        return Generator.generateExcel(groupToStudents, groups);
    }

    public static void registerStudent(StudentRegistrationArgs args) {
        return;
    }
}
