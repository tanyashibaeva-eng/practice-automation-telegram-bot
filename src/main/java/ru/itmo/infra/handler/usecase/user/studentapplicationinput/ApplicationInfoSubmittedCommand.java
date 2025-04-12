package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ApplicationInfoSubmittedCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Спасибо за информацию! Заявка отправлена на проверку преподавателю")
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
