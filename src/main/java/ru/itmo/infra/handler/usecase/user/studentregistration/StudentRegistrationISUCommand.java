package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationISUCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationProcessISUCommand());
        return MessageToUser.builder()
                .text("Введите ваш номер ИСУ (6-значное число)")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
