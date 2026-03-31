package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationConfirmationCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (UserRegistrationArgs) ContextHolder.getCommandData(chatId);

        args.setChatId(chatId);

        switch (message.getText()) {
            case "Да":
                var result = TelegramUserService.registerUser(args);
                if (result.getErrorText() == null || result.getErrorText().isEmpty()) {
                    ContextHolder.endCommand(chatId);
                    return MessageToUser.builder()
                            .text("Вы успешно зарегистрировались!")
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .needRewriting(false)
                            .build();
                } else {
                    ContextHolder.endCommand(chatId);
                    return MessageToUser.builder()
                            .text(result.getErrorText())
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .build();
                }
            case "Нет":
                ContextHolder.setNextCommand(chatId, new StudentRegistrationISUCommand());
                return MessageToUser.builder()
                        .text("Возврат в предыдущему шагу")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();

            case "Вернуться в меню":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Регистрация отменена")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Да\", \"Нет\" или \"Вернуться в меню\"")
                        .keyboardMarkup(getInlineKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}