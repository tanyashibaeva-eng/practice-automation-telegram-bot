package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
import ru.itmo.domain.model.Student;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.Parser;
import ru.itmo.infra.html.ParserIsuXls;
import ru.itmo.infra.storage.EduStreamRepository;
import ru.itmo.infra.storage.Filter;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.util.TextParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Log
public class StudentService {

    public static Optional<Student> findStudentByIsuAndEduStreamName(int isu, EduStream eduStream) throws InternalException {
        return StudentRepository.findByIsuAndEduStreamName(isu, eduStream);
    }

    public static Optional<File> updateStudentsFromExcel(File file, String eduStreamName) throws InternalException, BadRequestException {
        var eduStream = new EduStream(eduStreamName);
        var groups = EduStreamRepository.findAllGroupsByStreamName(eduStream);
        var students = StudentRepository.findAll(Filter.builder().eduStream(eduStream).build());
        var groupToStudentDTOsWithErrors = Parser.parseUpdateExcelFile(file, groups);

        if (students.isEmpty()) {
            return Optional.empty();
        }

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

        var hasErrors = false;
        for (var s : students) {
            var key = "%s-%s-%s".formatted(s.getIsu(), s.getFullName(), s.getStGroup());
            if (studentInfoToStudents.containsKey(key)) {
                var d = studentInfoToStudents.get(key);
                var errors = s.updateOrGetErrors(d);
                if (!errors.isEmpty()) {
                    hasErrors = true;
                    groupToStudentDTOsWithErrors.get(s.getStGroup()).getErrorsByRows().put(d.getRow(), errors);
                }
            }
        }

        if (hasErrors) {
            return Optional.of(Generator.generateExcelWithErrors(file, groupToStudentDTOsWithErrors));
        }

        StudentRepository.updateBatchByChatIdAndEduStreamName(students);
        return Optional.empty();
    }

    public static String createStudentsFromExcel(File file, String eduStreamName) throws InternalException, BadRequestException {
        var eduStream = new EduStream(eduStreamName);
        var parsedStudents = ParserIsuXls.parseISUXls(file);
        var studentsToCreate = new ArrayList<Student>();
        var errors = new StringBuilder();
        for (var s : parsedStudents) {
            if (!s.getErrors().isEmpty()) {
                errors.append("Строка: ").append(s.getRow()).append(" , Ошибки: ").append(String.join(", ", s.getErrors())).append("\n");
            }
            studentsToCreate.add(new Student(s, eduStream));
        }

        if (!errors.isEmpty()) {
            return errors.toString();
        }

        StudentRepository.saveBatch(studentsToCreate);
        return "";
    }

    public static File exportStudentsToExcel(String eduStreamName) throws InternalException, BadRequestException {
        var eduStream = new EduStream(eduStreamName);
        var groups = EduStreamRepository.findAllGroupsByStreamName(eduStream);
        var students = StudentRepository.findAll(Filter.builder().eduStream(eduStream).build());
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
    }

    public static Optional<Integer> validateIsu(String isuText) {
        try {
            var isu = TextParser.parseInt(isuText);
            return Optional.of(isu);
        } catch (BadRequestException e) {
            return Optional.empty();
        }
    }
}
