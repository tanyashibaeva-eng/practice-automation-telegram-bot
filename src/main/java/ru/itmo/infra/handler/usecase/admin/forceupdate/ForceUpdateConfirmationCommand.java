package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class ForceUpdateConfirmationCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (ForceUpdateDTO) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                var errors = StudentService.forceUpdateStudent(args);
                ContextHolder.endCommand(chatId);
                if (!errors.isEmpty()) {
                    return MessageToUser.builder()
                            .text("Ошибка во время ручного обновления студента: " + String.join(", ", errors))
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .build();
                }
                return MessageToUser.builder()
                        .text("Пользователь с chatId %d был изменен вручную".formatted(args.getChatId()))
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Возврат в главное меню")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Ответьте \"Да\" или \"Нет\"")
                        .keyboardMarkup(getConfirmationKeyboard())
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
