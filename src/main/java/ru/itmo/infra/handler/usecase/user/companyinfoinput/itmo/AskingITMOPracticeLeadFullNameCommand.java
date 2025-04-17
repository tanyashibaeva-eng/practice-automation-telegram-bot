package ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class AskingITMOPracticeLeadFullNameCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InputITMOStudentLeadFullNameCommand());
        return MessageToUser.builder()
                .text("Введите ФИО руководителя практики")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
