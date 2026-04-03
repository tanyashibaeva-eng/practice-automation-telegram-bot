package ru.itmo.infra.handler.usecase.admin.searchstudent;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.List;

public class SearchStudentByIsuCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ");

            if (fields.length < 2) {
                throw new BadRequestException("Формат: /search_by_isu <ISU номер>");
            }

            int isu;
            try {
                isu = TextUtils.parseIsu(fields[1]);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный формат ISU. Ожидается число");
            }

            String eduStreamName = ContextHolder.getEduStreamName(message.getChatId());

            List<Student> students = StudentService.findAllStudentsByIsuAndEduStreamName(isu, eduStreamName);
            if (students.isEmpty()) {
                throw new BadRequestException("Студент с ISU %d не найден в потоке %s".formatted(isu, eduStreamName));
            }

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(formatStudentList(students))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (UnknownUserException e) {
            return MessageToUser.builder()
                    .text("Сначала выберите поток через /start")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    static String formatStudentList(List<Student> students) {
        var sb = new StringBuilder("Результаты поиска:\n\n");
        for (var student : students) {
            appendStudentInfo(sb, student);
        }
        return sb.toString();
    }

    static void appendStudentInfo(StringBuilder sb, Student student) {
        sb.append("ФИО: ").append(student.getFullName()).append("\n");
        sb.append("ISU: ").append(student.getIsu()).append("\n");
        sb.append("Группа: ").append(student.getStGroup()).append("\n");
        sb.append("Статус: ").append(student.getStatus().getDisplayName()).append("\n");
        if (student.getPracticeFormat() != null && student.getPracticeFormat() != PracticeFormat.NOT_SPECIFIED) {
            sb.append("Формат практики: ").append(student.getPracticeFormat().getDisplayName()).append("\n");
        }
        if (student.getCompanyName() != null && !student.getCompanyName().isBlank()) {
            sb.append("Компания: ").append(student.getCompanyName()).append("\n");
        }
        if (student.getCompanyLeadFullName() != null && !student.getCompanyLeadFullName().isBlank()) {
            sb.append("Руководитель: ").append(student.getCompanyLeadFullName()).append("\n");
        }
        if (student.getCompanyLeadPhone() != null && !student.getCompanyLeadPhone().isBlank()) {
            sb.append("Телефон: ").append(student.getCompanyLeadPhone()).append("\n");
        }
        if (student.getCompanyLeadEmail() != null && !student.getCompanyLeadEmail().isBlank()) {
            sb.append("Email: ").append(student.getCompanyLeadEmail()).append("\n");
        }
        if (student.getCompanyLeadJobTitle() != null && !student.getCompanyLeadJobTitle().isBlank()) {
            sb.append("Должность: ").append(student.getCompanyLeadJobTitle()).append("\n");
        }
        if (student.getTelegramUser() != null) {
            sb.append("ChatId: ").append(student.getTelegramUser().getChatId()).append("\n");
        }
        sb.append("\n");
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/search_by_isu";
    }

    @Override
    public String getDescription() {
        return "Поиск студента по ISU. Пример: /search_by_isu 123456";
    }
}
