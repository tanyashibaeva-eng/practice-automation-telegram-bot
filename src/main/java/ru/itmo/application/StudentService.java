package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.command.InnValidationResult;
import ru.itmo.domain.dto.command.IsuValidationResult;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.client.NalogRuClient;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.GoogleSheetsExporter;
import ru.itmo.infra.excel.Parser;
import ru.itmo.infra.html.ParserIsuXls;
import ru.itmo.infra.storage.EduStreamRepository;
import ru.itmo.infra.storage.Filter;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.util.PropertiesProvider;
import ru.itmo.util.TextParser;

import java.io.File;
import java.io.IOException;
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

    public static IsuValidationResult validateIsu(String isuText, String eduStreamName) throws InternalException {
        try {
            var respBuilder = IsuValidationResult.builder();

            // парсим ису
            var isu = TextParser.parseIsu(isuText);
            respBuilder.isu(isu);

            // проверяем что такой студент есть
            var eduStream = new EduStream(eduStreamName);
            var studentOpt = StudentRepository.findByIsuAndEduStreamName(isu, eduStream);
            if (studentOpt.isEmpty()) {
                respBuilder.errorText("Студент с ИСУ %d не найден в потоке %s, попробуйте еще раз".formatted(isu, eduStreamName));
                return respBuilder.build();
            }
            var student = studentOpt.get();
            respBuilder.student(student);

            // проверяем зарегистрирован ли он уже
            if (student.getStatus() != StudentStatus.NOT_REGISTERED) {
                respBuilder.alreadyRegistered(true);
                return respBuilder.build();
            }

            return respBuilder.build();
        } catch (BadRequestException e) {
            return IsuValidationResult.builder().errorText(e.getMessage()).build();
        } catch (InternalException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage());
        }
    }

    public static InnValidationResult validateInn(String inn) throws InternalException {
        try {
            var respBuilder = InnValidationResult.builder();

            // парсим инн
            long innLong;
            try {
                innLong = TextParser.parseDoubleToLong(inn);
                respBuilder.inn(innLong);
            } catch (BadRequestException e) {
                return InnValidationResult.builder().errorText("ИНН должен быть числом").build();
            }

            // валидируем инн
            if (inn.length() != 10) {
                respBuilder.errorText("ИНН должен состоять из 10");
                return respBuilder.build();
            }

            // если включена опция проверки ИНН на налог.ру – пытаемся найти и проставить компанию
            if (PropertiesProvider.getInnCheck()) {
                var companyName = NalogRuClient.getCompanyNameByInn(inn);
                respBuilder.companyName(companyName);
            }

            // если компания не найдена/опция отключена – просим заполнить компанию
            if (respBuilder.build().getCompanyName() == null) {
                respBuilder.userShouldProvideCompanyName(true);
                return respBuilder.build();
            }

            // проставляем флаг для питерских компаний
            respBuilder.isSPB(inn.trim().startsWith("78"));

            // проверяем в списке компаний с договорами
            respBuilder.isPresentInITMOAgreementFile(GoogleSheetsExporter.checkInnInCsv(innLong));

            return respBuilder.build();
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage());
        }
    }
}
