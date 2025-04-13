package ru.itmo.infra.handler.usecase.user.studentstatus;

import lombok.SneakyThrows;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class StatusCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {

        return MessageToUser.builder()
                .text("Информация о статусе студента")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/status";
    }

    @Override
    public String getDescription() {
        return "Узнать статус";
    }
}
