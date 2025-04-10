package ru.itmo.infra.handler.usecase.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ApplicationInfoTakenCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Спасибо за информацию! Заявка на проверке у преподавателя")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/application_done";
    }
}
