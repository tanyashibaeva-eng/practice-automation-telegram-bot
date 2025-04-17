package ru.itmo.infra.handler.usecase.admin.studentinfo;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class GetStudentInfoCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ");

            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды. не указан chatId студента, формат:\n" +
                        "/student_info <chatID>");
            }

            long chatId;
            try {
                chatId = TextUtils.parseDoubleStrToLong(fields[1]);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный формат <chatId>. Ожидается число");
            }

            var students = StudentService.getStudentsByChatId(chatId);
            if (students.isEmpty()) {
                throw new BadRequestException("Студент с chatId %d не найден".formatted(chatId));
            }

            var textBuilder = new StringBuilder();
            textBuilder.append("Найденные записи о студенте:\n\n");

            for (var student : students) {
                textBuilder.append("Поток: ").append(student.getEduStream().getName()).append("\n");
                textBuilder.append("ФИО: ").append(student.getFullName()).append("\n");
                textBuilder.append("ИСУ: ").append(student.getIsu()).append("\n");
                textBuilder.append("Группа: ").append(student.getStGroup()).append("\n");
                textBuilder.append("Статус: ").append(student.getStatus().getDisplayName()).append("\n");

                if (student.getPracticeFormat() != null && student.getPracticeFormat() != PracticeFormat.NOT_SPECIFIED) {
                    textBuilder.append("Формат практики: ").append(student.getPracticeFormat().getDisplayName()).append("\n");
                }
                if (student.getCompanyName() != null  && !student.getCompanyName().isBlank()) {
                    textBuilder.append("Компания: ").append(student.getCompanyName()).append("\n");
                }
                if (student.getCompanyINN() != null) {
                    textBuilder.append("ИНН Компании: ").append(student.getCompanyINN()).append("\n");
                }
                if (student.getCompanyLeadFullName() != null && !student.getCompanyLeadFullName().isBlank()) {
                    textBuilder.append("Руководитель: ").append(student.getCompanyLeadFullName()).append("\n");
                }
                if (student.getCompanyLeadPhone() != null) {
                    textBuilder.append("Телефон руководителя: ").append(student.getCompanyLeadPhone()).append("\n");
                }
                if (student.getCompanyLeadEmail() != null) {
                    textBuilder.append("Почта руководителя: ").append(student.getCompanyLeadEmail()).append("\n");
                }
                if (student.getCompanyLeadJobTitle() != null && !student.getCompanyLeadJobTitle().isBlank()) {
                    textBuilder.append("Должность руководителя: ").append(student.getCompanyLeadJobTitle()).append("\n");
                }
                textBuilder.append("\n");
            }

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(textBuilder.toString())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/student_info";
    }

    @Override
    public String getDescription() {
        return "Получить информацию о студенте по chatId. Пример: `/student_info 27263272`";
    }
}