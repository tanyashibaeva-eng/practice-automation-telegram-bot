package ru.itmo.infra.handler.usecase.studentapplicationinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ErrorApplicationInputCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.endCommand(chatId);
        return MessageToUser.builder()
                .text("Введенные данные для заявки не корректны. Попробуйте еще раз")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/application_error";
    }
}
