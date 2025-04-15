package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationProcessISUCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var isuNumber = message.getText().trim();
        var chatId = message.getChatId();
        // Получаем сохраненное имя потока из ContextHolder
        var eduStreamName = ContextHolder.getEduStreamName(chatId);
        var isuResp = StudentService.validateIsu(isuNumber, eduStreamName);

        if (isuResp.getErrorText() != null) {
            ContextHolder.setNextCommand(chatId, new StudentRegistrationProcessISUCommand());
            return MessageToUser.builder()
                    .text(isuResp.getErrorText())
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var student = isuResp.getStudent();
        var dto = UserRegistrationArgs.builder()
                .username(student.getFullName())
                .isu(isuResp.getIsu())
                .eduStreamName(eduStreamName) // Сохраняем имя потока в DTO
                .build();
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
        return MessageToUser.builder()
                .text("Найден студент с ИСУ номером %d. Его ФИО: %s. Поток: %s. Это вы?".formatted(
                        student.getIsu(),
                        student.getFullName(),
                        eduStreamName))
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}