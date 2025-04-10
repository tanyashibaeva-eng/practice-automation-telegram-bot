package ru.itmo.infra.handler.usecase.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.util.TextParser;

public class StudentRegistrationProcessISUCommand implements Command {
    public static final TextParser textParser = new TextParser();

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var isuNumber = message.getText().trim();
        var isuResp = StudentService.validateIsu(isuNumber, "1");
        var chatId = message.getChatId();
        if (isuResp.getErrorText() != null) {
            ContextHolder.setNextCommand(chatId, new StudentRegistrationISUCommand());
            return MessageToUser.builder()
                    .text(isuResp.getErrorText())
                    .build();
        }

        var student = isuResp.getStudent();
        var dto = UserRegistrationArgs.builder().isu(isuResp.getIsu()).build(); // TODO: подумать возможно класть просто студента
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
        return MessageToUser.builder()
                .text("Найден студент с ИСУ номером %d. Его ФИО: %s. Это вы?".formatted(student.getIsu(), student.getFullName()))
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
