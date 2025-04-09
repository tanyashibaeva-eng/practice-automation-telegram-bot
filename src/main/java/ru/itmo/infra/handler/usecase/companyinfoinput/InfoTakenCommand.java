package ru.itmo.infra.handler.usecase.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class InfoTakenCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Спасибо за информацию! Данные о практике на проверке у преподавателя")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/practice_done";
    }
}