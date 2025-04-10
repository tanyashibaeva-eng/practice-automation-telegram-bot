package ru.itmo.infra.handler.usecase.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class FacultyCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new CodeAndNameSpecializationCommand());

        return MessageToUser.builder()
                .text("Введите наименование вашего факультета")
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
        return "/faculty";
    }
}
