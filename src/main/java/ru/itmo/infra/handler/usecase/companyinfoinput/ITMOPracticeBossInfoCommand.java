package ru.itmo.infra.handler.usecase.companyinfoinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ITMOPracticeBossInfoCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InfoTakenCommand());

        return MessageToUser.builder()
                .text("Введите данные научного руководителя (в формате)")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/who_itmo";
    }
}
