package ru.itmo.infra.handler.usecase.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationConfirmationCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (StudentRegistrationArgs) ContextHolder.getCommandData(chatId);
        args.setChatId(chatId);
        switch (message.getText()) {
            case "Да":
                StudentService.registerStudent(args);
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder().text("Вы были успешно зарегистрированы").build();
            case "Нет":
                ContextHolder.setCommandData(chatId, new StudentRegistrationProcessISUCommand());
                return MessageToUser.builder().text("Возврат в предыдущему шагу").build();
            case "Вернуться в меню":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder().text("Регистрация отменена").build();
            default:
                ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Да\", \"Нет\" или \"Вернуться в меню\"")
                        .keyboardMarkup(StudentRegistrationProcessISUCommand.getInlineKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
