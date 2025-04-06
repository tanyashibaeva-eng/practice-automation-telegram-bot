package ru.itmo.infra.handler.usecase.studentregistration;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationStartCommand implements Command {

    @Override
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationProcessISUCommand());
        return MessageToUser.builder()
                .text("Введите ваш номер ИСУ")
                .keyboardMarkup(getReturnToStartMarkup())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/register";
    }
}
