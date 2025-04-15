package ru.itmo.application;

import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import ru.itmo.domain.dto.ApplicationDTO;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.domain.dto.command.*;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.client.NalogRuClient;
import ru.itmo.infra.docx.DocxGenerator;
import ru.itmo.infra.excel.Generator;
import ru.itmo.infra.excel.GoogleSheetsExporter;
import ru.itmo.infra.excel.Parser;
import ru.itmo.infra.html.ParserIsuXls;
import ru.itmo.infra.storage.EduStreamRepository;
import ru.itmo.infra.storage.Filter;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.util.EduStreamChecker;
import ru.itmo.util.PropertiesProvider;
import ru.itmo.util.TextParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Log
public class StudentService {

    public static List<Student> findAllStudentsByIsuAndEduStreamName(int isu, String eduStreamName) throws InternalException, BadRequestException {
        return StudentRepository.findAllByIsuAndEduStreamName(isu, new EduStream(eduStreamName));
    }

    public static Optional<Student> findStudentByChatIdAndEduStreamName(long chatId, String eduStreamName) throws InternalException, BadRequestException {
        return StudentRepository.findByChatIdAndEduStreamName(chatId, new EduStream(eduStreamName));
    }

    public static Optional<String> findActiveEduStreamNameByChatId(long chatId) throws InternalException {
        List<Student> students = StudentRepository.findAllByChatId(chatId);
        for (var student : students) {
            if (EduStreamChecker.isActiveStream(student.getEduStream()))
                return Optional.of(student.getEduStream().getName());
        }
        return Optional.empty();
    }

    public static Optional<String> getNewestStudentEduStreamNameByChatId(long chatId) throws InternalException {
        List<Student> students = StudentRepository.findAllByChatId(chatId);
        EduStream curr = null;
        for (var student : students) {
            if (curr == null) {
                curr = student.getEduStream();
                continue;
            }
            if (student.getEduStream().getDateTo().isAfter(curr.getDateTo())) {
                curr = student.getEduStream();
            }
        }
        if (curr != null) {
            return Optional.of(curr.getName());
        }
        return Optional.empty();
    }

    /* Здесь мы считаем, что студент существует, имеет право на обновление данных о компании (статус соответствует),
       ИНН валиден и соотносится с форматом прохождения практики, все остальные поля валидны */
    public static boolean updateCompanyInfo(CompanyInfoUpdateArgs args) throws InternalException, BadRequestException {
        Optional<String> eduStreamNameOpt = findActiveEduStreamNameByChatId(args.getChatId());
        if (eduStreamNameOpt.isEmpty())
            throw new BadRequestException("Студент не находится ни в одном активном потоке");
        return StudentRepository.updateCompanyInfo(args, eduStreamNameOpt.get());
    }

    /* Здесь мы считаем, что студент существует, имеет право на обновление данных о практике в ИТМО (статус соответствует),
       название подразделения и ФИО руководителя валидны */
    public static boolean updateITMOPracticeInfo(ITMOPracticeInfoUpdateArgs args) throws InternalException, BadRequestException {
        Optional<String> eduStreamNameOpt = findActiveEduStreamNameByChatId(args.getChatId());
        if (eduStreamNameOpt.isEmpty())
            throw new BadRequestException("Студент не находится ни в одном активном потоке");
        return StudentRepository.updateITMOPracticeInfo(args, eduStreamNameOpt.get());
    }

    public static File getApplicationFile(long chatId) throws InternalException, BadRequestException {
        try {
            List<Student> students = StudentRepository.findAllByChatId(chatId);
            for (var student : students) {
                if (EduStreamChecker.isActiveStream(student.getEduStream())) {
                    if (student.getApplicationBytes() == null) {
                        throw new BadRequestException("Нельзя выкачать заявку для студента %s, так как она еще не загружена".formatted(student.getFullName()));
                    }
                    var application = new File("Заявка - %s.docx".formatted(student.getFullName()));
                    FileUtils.writeByteArrayToFile(application, student.getApplicationBytes());
                    return application;
                }
            }
            throw new BadRequestException("Невозможно выкачать заявку, так как студент с chatId %d не найден".formatted(chatId));
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: заявка не может быть выгружена");
        }
    }

    public static boolean updateApplicationBytesByChatIdAndEduStreamName(long chatId, String eduStreamName, File application) throws InternalException {
        byte[] applicationBytes;

        try {
            applicationBytes = Files.readAllBytes(application.toPath());
        } catch (IOException ex) {
            log.severe("Ошибка чтения файла: " + ex.getMessage());
            throw new InternalException("Что-то пошло не так");
        }

        return updateApplicationBytesByChatIdAndEduStreamName(chatId, eduStreamName, applicationBytes);
    }

    public static boolean updateApplicationBytesByChatIdAndEduStreamName(long chatId, String eduStreamName, byte[] newBytes) throws InternalException {
        return StudentRepository.updateApplicationBytesByChatIdAndEduStreamName(chatId, eduStreamName, newBytes);
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
                studentInfoToStudents.put("%s-%d-%s-%s".formatted(d.getChatId() == null ? "" : d.getChatId(), d.getIsu(), d.getFullName(), d.getStGroup()), d);
            }
        }

        var hasErrors = false;
        for (var s : students) {
            var key = "%s-%d-%s-%s".formatted(s.getTelegramUser() == null ? "" : s.getTelegramUser().getChatId(), s.getIsu(), s.getFullName(), s.getStGroup());
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

        students.stream().filter(Student::isPingNeeded).forEach(NotificationService::pingStudent);

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

        StudentRepository.saveBaseBatch(studentsToCreate);
        return "";
    }

    public static File exportStudentsToExcel(String eduStreamName) throws InternalException, BadRequestException {
        var eduStream = new EduStream(eduStreamName);
        var groups = EduStreamRepository.findAllGroupsByStreamName(eduStream);
        var students = StudentRepository.exportAll(eduStream);
        var groupToStudents = new HashMap<String, List<Student>>();

        for (var s : students) {
            if (!groupToStudents.containsKey(s.getStGroup())) {
                groupToStudents.put(s.getStGroup(), new ArrayList<>());
            }
            groupToStudents.get(s.getStGroup()).add(s);
        }

        return Generator.generateExcel(groupToStudents, groups);
    }

    public static IsuValidationResult validateIsu(String isuText, String eduStreamName) throws InternalException {
        try {
            var resBuilder = IsuValidationResult.builder();

            // парсим ису
            var isu = TextParser.parseIsu(isuText);
            resBuilder.isu(isu);

            // проверяем что такой студент есть
            var eduStream = new EduStream(eduStreamName);
            var studentList = StudentRepository.findAllByIsuAndEduStreamName(isu, eduStream);
            if (studentList.isEmpty()) {
                resBuilder.errorText("Студент с ИСУ %d не найден в потоке %s, попробуйте еще раз".formatted(isu, eduStreamName));
                return resBuilder.build();
            }
            var student = studentList.get(0).duplicateBase();
            resBuilder.student(student);

            return resBuilder.build();
        } catch (BadRequestException e) {
            return IsuValidationResult.builder().errorText(e.getMessage()).build();
        } catch (InternalException e) {
            log.severe("Ошибка во время валидации ИСУ: " + e.getMessage());
            throw new InternalException("Что-то пошло не так");
        }
    }

    public static InnValidationResult validateInn(String inn) throws InternalException {
        try {
            var resBuilder = InnValidationResult.builder();

            // парсим инн
            long innLong;
            try {
                innLong = TextParser.parseDoubleStrToLong(inn);
                resBuilder.inn(innLong);
            } catch (BadRequestException e) {
                return InnValidationResult.builder().errorText("ИНН должен быть числом").build();
            }

            // валидируем инн
            if (inn.length() != 10) {
                resBuilder.errorText("ИНН должен состоять из 10");
                return resBuilder.build();
            }

            // если включена опция проверки ИНН на налог.ру – пытаемся найти и проставить компанию
            if (PropertiesProvider.getInnCheck()) {
                var companyName = NalogRuClient.getCompanyNameByInn(inn);
                resBuilder.companyName(companyName);
            }

            // если компания не найдена/опция отключена – просим заполнить компанию
            if (resBuilder.build().getCompanyName() == null) {
                resBuilder.userShouldProvideCompanyName(true);
            }

            // проставляем флаг для питерских компаний
            resBuilder.isSPB(inn.trim().startsWith("78"));

            // проверяем в списке компаний с договорами
            resBuilder.isPresentInITMOAgreementFile(GoogleSheetsExporter.checkInnInCsv(innLong));

            return resBuilder.build();
        } catch (IOException e) {
            throw new InternalException("Произошла техническая ошибка: " + e.getMessage());
        }
    }

    public static PracticeFormatValidationResult validatePracticeFormat(InnValidationResult innValidationResult, PracticeFormat practiceFormat) {
        if (innValidationResult.isSPB() || practiceFormat == PracticeFormat.ONLINE)
            return PracticeFormatValidationResult.builder()
                    .errorText("")
                    .build();
        return PracticeFormatValidationResult.builder()
                .errorText("Для компаний не из Санкт-Петербурга формат прохождения практики может быть только дистанционным")
                .build();
    }

    public static ApplicationFillingResult generateApplicationFileByChatId(long chatId) throws InternalException {
        // ищем студента в активных потоках
        var students = StudentRepository.findAllByChatId(chatId);
        Student currStudent = null;
        for (var student : students) {
            if (EduStreamChecker.isActiveStream(student.getEduStream())) {
                currStudent = student;
                break;
            }
        }

        var resBuilder = ApplicationFillingResult.builder();

        // если не нашли – значит не генерируем заявку
        if (currStudent == null) {
            resBuilder.errorText("Студент с таким чат айди не найден в активных потоках");
            return resBuilder.build();
        }

        // проверяем что студент в нужном статусе
        if (!AuthorizationService.canStudentDownloadApplication(chatId)) {
            resBuilder.errorText("Невозможно заполнить заявку для студента в статусе \"%s\"".formatted(currStudent.getStatus().getDisplayName()));
            return resBuilder.build();
        }

        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        var file = DocxGenerator.fillApplicationTemplate(new ApplicationDTO(
                currStudent.getFullName(),
                currStudent.getStGroup(),
                currStudent.getEduStream().getDateFrom().format(formatter),
                currStudent.getEduStream().getDateTo().format(formatter),
                currStudent.getPracticeFormat() == PracticeFormat.OFFLINE ? "Очно" : "С применением дистанционных технологий",
                currStudent.getCompanyName()
        ));
        return resBuilder.file(file).build();
    }

    public static List<Student> getStudentsByChatId(long chatId) throws InternalException {
        return StudentRepository.findAllByChatId(chatId);
    }

    public static List<String> forceUpdateStudent(ForceUpdateDTO dto) throws InternalException {
        try {
            var eduStreamName = EduStreamRepository.findByName(new EduStream(dto.getEduStreamName()));
            if (eduStreamName.isEmpty()) {
                throw new BadRequestException("Поток с именем %s не найден".formatted(dto.getEduStreamName()));
            }

            var stOpt = StudentRepository.findByChatIdAndEduStreamName(dto.getChatId(), eduStreamName.get());
            if (stOpt.isEmpty()) {
                throw new BadRequestException("Студент с chatId %d не найден в потоке %s".formatted(dto.getChatId(), dto.getEduStreamName()));
            }

            var student = stOpt.get();
            var errors = student.forceUpdateOrGetErrors(dto);
            if (!errors.isEmpty()) {
                return errors;
            }

            StudentRepository.updateBatchByChatIdAndEduStreamName(List.of(student));
            return List.of();
        } catch (BadRequestException e) {
            return List.of(e.getMessage());
        }
    }

}
